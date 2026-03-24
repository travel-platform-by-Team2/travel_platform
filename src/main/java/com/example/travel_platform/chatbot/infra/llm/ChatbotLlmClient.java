package com.example.travel_platform.chatbot.infra.llm;

import java.util.List;

import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.application.ChatbotService;

public interface ChatbotLlmClient {

    ChatbotService.Interpretation interpret(String userMessage, ChatbotRequest.ContextDTO context, String toolContext);

    String answerGeneralChat(String userMessage, ChatbotRequest.ContextDTO context);

    String answerDbQa(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            ChatbotService.Interpretation interpretation,
            List<ChatbotService.QueryBlock> queryBlocks);
}
