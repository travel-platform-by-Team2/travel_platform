package com.example.travel_platform.chatbot.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmSearchReview;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotPlanService {

    private final ChatbotLlmClient chatbotLlmClient;
    private final ChatSchemaProvider chatSchemaProvider;

    public ChatbotLlmPlan createPlan(String message, ChatbotRequest.ContextDTO context) {
        try {
            return chatbotLlmClient.createPlan(message, context, chatSchemaProvider.getSchemaContext());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM 질의 계획 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    public ChatbotLlmSearchReview reviewSearch(
            String message,
            ChatbotRequest.ContextDTO context,
            String queryIntent,
            List<ChatbotSearchAttempt> searchAttempts,
            int maxSearchAttempts) {
        try {
            return chatbotLlmClient.reviewSearch(
                    message,
                    context,
                    queryIntent,
                    searchAttempts,
                    maxSearchAttempts,
                    chatSchemaProvider.getSchemaContext());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM 탐색 재평가 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }
}
