package com.example.travel_platform.chatbot.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.example.travel_platform.chatbot.application.ChatbotService;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class ChatbotApiControllerTest {

    private MockMvc mockMvc;
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = mock(ChatbotService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatbotApiController(chatbotService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void ask_returnsChatbotAnswer_whenLoggedIn() throws Exception {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.createAskResponse(
                "DB_QA",
                "예약이 2건 있어요.",
                java.util.List.of("BOOKING"),
                true);
        given(chatbotService.ask(eq(1), any(ChatbotRequest.AskDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/chatbot/messages")
                .sessionAttr(SessionUsers.SESSION_USER_KEY, new SessionUser(1, "ssar", "a@a.com", "010", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "message": "내 예약 알려줘",
                          "context": {
                            "page": "/mypage"
                          }
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.answer").value("예약이 2건 있어요."))
                .andExpect(jsonPath("$.body.mode").value("DB_QA"))
                .andExpect(jsonPath("$.body.processingType").value("DB_QA"))
                .andExpect(jsonPath("$.body.usedTools[0]").value("BOOKING"))
                .andExpect(jsonPath("$.body.hasSufficientData").value(true));
    }

    @Test
    void ask_returnsUnauthorized_whenSessionUserMissing() throws Exception {
        mockMvc.perform(post("/api/chatbot/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "message": "내 예약 알려줘"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("API_UNAUTHORIZED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(containsString("로그인이 필요")));

        verifyNoInteractions(chatbotService);
    }

    @Test
    void ask_returnsBadRequest_whenMessageBlank() throws Exception {
        mockMvc.perform(post("/api/chatbot/messages")
                .sessionAttr(SessionUsers.SESSION_USER_KEY, new SessionUser(1, "ssar", "a@a.com", "010", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "message": "   "
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("API_BAD_REQUEST"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(chatbotService);
    }

    @Test
    void ask_returnsInternalServerError_whenServiceThrows() throws Exception {
        given(chatbotService.ask(eq(1), any(ChatbotRequest.AskDTO.class)))
                .willThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "boom", HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/api/chatbot/messages")
                .sessionAttr(SessionUsers.SESSION_USER_KEY, new SessionUser(1, "ssar", "a@a.com", "010", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "message": "예약 알려줘"
                        }
                        """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("CHATBOT_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("boom"));
    }
}
