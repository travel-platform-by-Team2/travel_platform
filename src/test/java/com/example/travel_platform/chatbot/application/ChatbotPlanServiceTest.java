package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmSearchReview;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

class ChatbotPlanServiceTest {

    @Test
    void plan() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatSchemaProvider chatSchemaProvider = mock(ChatSchemaProvider.class);
        ChatbotPlanService service = new ChatbotPlanService(chatbotLlmClient, chatSchemaProvider);
        ChatbotRequest.ContextDTO context = ChatbotRequest.ContextDTO.of("/trip-detail", 3);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(true, "TRIP", "요약", "select id from trip_plan_tb limit 5", "");

        when(chatSchemaProvider.getSchemaContext()).thenReturn("{\"tables\":[]}");
        when(chatbotLlmClient.createPlan("trip", context, "{\"tables\":[]}")).thenReturn(plan);

        ChatbotLlmPlan response = service.createPlan("trip", context);

        assertSame(plan, response);
        verify(chatbotLlmClient).createPlan("trip", context, "{\"tables\":[]}");
    }

    @Test
    void review() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatSchemaProvider chatSchemaProvider = mock(ChatSchemaProvider.class);
        ChatbotPlanService service = new ChatbotPlanService(chatbotLlmClient, chatSchemaProvider);
        ChatbotRequest.ContextDTO context = ChatbotRequest.ContextDTO.of("/trip-detail", 3);
        ChatbotLlmSearchReview review = new ChatbotLlmSearchReview(false, "TRIP", "", "", "충분함");
        List<ChatbotSearchAttempt> attempts = List.of();

        when(chatSchemaProvider.getSchemaContext()).thenReturn("{\"tables\":[]}");
        when(chatbotLlmClient.reviewSearch("trip", context, "TRIP", attempts, 5, "{\"tables\":[]}")).thenReturn(review);

        ChatbotLlmSearchReview response = service.reviewSearch("trip", context, "TRIP", attempts, 5);

        assertSame(review, response);
        verify(chatbotLlmClient).reviewSearch("trip", context, "TRIP", attempts, 5, "{\"tables\":[]}");
    }
}
