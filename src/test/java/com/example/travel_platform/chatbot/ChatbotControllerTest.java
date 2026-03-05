package com.example.travel_platform.chatbot;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.travel_platform.chatbot.exception.ChatbotException;
import com.example.travel_platform.chatbot.exception.ChatbotExceptionHandler;

@WebMvcTest(ChatbotController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ChatbotExceptionHandler.class)
class ChatbotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatbotService chatbotService;

    @Test
    void ask_success_returnsOkJson() throws Exception {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.builder()
                .processingType("DIRECT_LLM")
                .answer("ok")
                .meta(ChatbotResponse.MetaDTO.builder()
                        .needsDb(false)
                        .build())
                .build();
        given(chatbotService.ask(any(ChatbotRequest.AskDTO.class))).willReturn(response);

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
                .andExpect(jsonPath("$.processingType").value("DIRECT_LLM"))
                .andExpect(jsonPath("$.answer").value("ok"))
                .andExpect(jsonPath("$.meta.needsDb").value(false));
    }

    @Test
    void ask_blankMessage_returnsBadRequestJson() throws Exception {
        String requestBody = """
                {
                  "message": "   "
                }
                """;

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHATBOT_BAD_REQUEST"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("must not be blank")))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(chatbotService);
    }

    @Test
    void ask_serviceError_returnsInternalErrorJson() throws Exception {
        given(chatbotService.ask(any(ChatbotRequest.AskDTO.class)))
                .willThrow(new ChatbotException("CHATBOT_INTERNAL_ERROR", "boom", HttpStatus.INTERNAL_SERVER_ERROR));

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
    void ask_malformedJson_returnsBadRequestJson() throws Exception {
        String requestBody = "{ \"message\": \"hello\" ";

        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHATBOT_BAD_REQUEST"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request JSON body is invalid."))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
