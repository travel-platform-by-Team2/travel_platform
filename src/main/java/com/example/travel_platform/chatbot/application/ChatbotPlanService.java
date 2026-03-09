package com.example.travel_platform.chatbot.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;

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
}
