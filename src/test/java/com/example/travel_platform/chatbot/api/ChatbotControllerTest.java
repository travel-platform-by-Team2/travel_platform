package com.example.travel_platform.chatbot.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.travel_platform._core.handler.ApiExceptionHandler;
import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.application.ChatbotOrchestrator;

class ChatbotControllerTest {

    private MockMvc mockMvc;
    private ChatbotOrchestrator chatbotOrchestrator;

    @BeforeEach
    void setUp() {
        chatbotOrchestrator = mock(ChatbotOrchestrator.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatbotController(chatbotOrchestrator))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void ok() throws Exception {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.createAskResponse(
                "DIRECT_LLM",
                "ok",
                ChatbotResponse.MetaDTO.createDirectMeta());
        given(chatbotOrchestrator.ask(any(ChatbotRequest.AskDTO.class))).willReturn(response);

        String requestBody = """
                {
                  "message": "hello",
                  "context": {
                    "page": "/main"
                  }
                }
                """;

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.processingType").value("DIRECT_LLM"))
                .andExpect(jsonPath("$.body.answer").value("ok"))
                .andExpect(jsonPath("$.body.meta.needsDb").value(false));
    }

    @Test
    void blank() throws Exception {
        String requestBody = """
                {
                  "message": "   "
                }
                """;

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("API_BAD_REQUEST"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("must not be blank")))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(chatbotOrchestrator);
    }

    @Test
    void svcErr() throws Exception {
        given(chatbotOrchestrator.ask(any(ChatbotRequest.AskDTO.class)))
                .willThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "boom", HttpStatus.INTERNAL_SERVER_ERROR));

        String requestBody = """
                {
                  "message": "booking list"
                }
                """;

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("CHATBOT_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("boom"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void badJson() throws Exception {
        String requestBody = "{ \"message\": \"hello\" ";

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("API_BAD_REQUEST"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request JSON body is invalid."))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

