package com.example.travel_platform.chatbot;

import lombok.Data;

public class ChatbotRequest {

    @Data
    public static class AskDTO {
        private String message;
        private ContextDTO context;
    }

    @Data
    public static class ContextDTO {
        private String page;
        private Integer tripPlanId;
    }
}
