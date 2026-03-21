package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

class ChatbotAnswerServiceTest {

    @Test
    void direct() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatbotAnswerService service = new ChatbotAnswerService(chatbotLlmClient);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(false, "GENERAL_CHAT", "", "", "");

        assertEquals("답변을 생성하지 못했습니다.", service.resolveDirectAnswer(plan));
    }

    @Test
    void db() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatbotAnswerService service = new ChatbotAnswerService(chatbotLlmClient);
        List<ChatbotSearchAttempt> attempts = List.of();

        when(chatbotLlmClient.createDbAnswer("hello", "GENERAL_CHAT", attempts, false)).thenReturn("ok");

        String answer = service.createDbAnswer("hello", "", attempts, false);

        assertEquals("ok", answer);
        verify(chatbotLlmClient).createDbAnswer("hello", "GENERAL_CHAT", attempts, false);
    }
}

