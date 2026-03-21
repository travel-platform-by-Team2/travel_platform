package com.example.travel_platform.chatbot.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ChatbotRequest {

    @Data
    public static class AskDTO {
        @NotBlank
        private String message;

        @Valid
        private ContextDTO context;

        public static AskDTO createAskRequest(String message, ContextDTO context) {
            AskDTO dto = new AskDTO();
            dto.setMessage(message);
            dto.setContext(context);
            return dto;
        }
    }

    @Data
    public static class ContextDTO {
        private String page;
        private Integer tripPlanId;

        public static ContextDTO createContext(String page, Integer tripPlanId) {
            ContextDTO dto = new ContextDTO();
            dto.setPage(page);
            dto.setTripPlanId(tripPlanId);
            return dto;
        }
    }
}

