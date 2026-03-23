package com.example.travel_platform.chatbot.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public class ChatbotResponse {

    @Data
    public static class AskDTO {
        private String processingType;
        private String mode;
        private String answer;
        private List<String> usedTools;
        private Boolean hasSufficientData;

        public static AskDTO createAskResponse(
                String mode,
                String answer,
                List<String> usedTools,
                Boolean hasSufficientData) {
            AskDTO dto = new AskDTO();
            dto.setProcessingType(mode);
            dto.setMode(mode);
            dto.setAnswer(answer);
            dto.setUsedTools(copyUsedTools(usedTools));
            dto.setHasSufficientData(hasSufficientData);
            return dto;
        }

        private static List<String> copyUsedTools(List<String> usedTools) {
            if (usedTools == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(usedTools);
        }
    }
}
