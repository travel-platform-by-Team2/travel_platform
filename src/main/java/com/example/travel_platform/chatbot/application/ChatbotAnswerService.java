package com.example.travel_platform.chatbot.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotAnswerService {

    private static final String INTENT_GENERAL_CHAT = "GENERAL_CHAT";
    private static final String DEFAULT_DIRECT_ANSWER = "답변을 생성하지 못했습니다.";

    private final ChatbotLlmClient chatbotLlmClient;

    public String resolveDirectAnswer(ChatbotLlmPlan llmPlan) {
        return resolveAnswerText(llmPlan.answer(), DEFAULT_DIRECT_ANSWER);
    }

    public String createDbAnswer(
            String message,
            String queryIntent,
            List<ChatbotSearchAttempt> searchAttempts,
            boolean exhausted) {
        try {
            return chatbotLlmClient.createDbAnswer(
                    message,
                    resolveQueryIntent(queryIntent),
                    searchAttempts,
                    exhausted);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM DB 답변 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private String resolveQueryIntent(String queryIntent) {
        if (queryIntent == null || queryIntent.isBlank()) {
            return INTENT_GENERAL_CHAT;
        }
        return queryIntent;
    }

    private String resolveAnswerText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
