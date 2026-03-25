package com.example.travel_platform.chatbot;

import java.util.ArrayList;
import java.util.List;

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

        @Valid
        private List<HistoryItemDTO> history;

        public static AskDTO createAskRequest(String message, ContextDTO context) {
            return createAskRequest(message, context, List.of());
        }

        public static AskDTO createAskRequest(String message, ContextDTO context, List<HistoryItemDTO> history) {
            AskDTO dto = new AskDTO();
            dto.setMessage(message);
            dto.setContext(context);
            dto.setHistory(history == null ? List.of() : new ArrayList<>(history));
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

    @Data
    public static class HistoryItemDTO {
        private String role;
        private String content;

        public static HistoryItemDTO createHistoryItem(String role, String content) {
            HistoryItemDTO dto = new HistoryItemDTO();
            dto.setRole(role);
            dto.setContent(content);
            return dto;
        }
    }
}

