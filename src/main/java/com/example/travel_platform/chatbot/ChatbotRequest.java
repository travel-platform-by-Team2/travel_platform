package com.example.travel_platform.chatbot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 챗봇 요청 바디 DTO를 정의하는 클래스
public class ChatbotRequest {

    @Data
    public static class AskDTO {
        @NotBlank
        private String message;

        @Valid
        private ContextDTO context;
    }

    @Data
    public static class ContextDTO {
        private String page;
        private Integer tripPlanId;
    }
}
