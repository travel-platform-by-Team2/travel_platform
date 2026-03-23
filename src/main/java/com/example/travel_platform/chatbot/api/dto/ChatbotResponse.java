package com.example.travel_platform.chatbot.api.dto;

import lombok.Builder;
import lombok.Data;

public class ChatbotResponse {

    @Data
    @Builder
    public static class AskDTO {
        private String processingType;
        private String answer;
        private MetaDTO meta;

        public static AskDTO createAskResponse(String processingType, String answer, MetaDTO meta) {
            return AskDTO.builder()
                    .processingType(processingType)
                    .answer(answer)
                    .meta(meta)
                    .build();
        }
    }

    @Data
    @Builder
    public static class MetaDTO {
        private Boolean needsDb;
        private String querySummary;
        private String generatedSql;
        private Integer rowCount;

        public static MetaDTO createDirectMeta() {
            return MetaDTO.builder()
                    .needsDb(false)
                    .build();
        }

        public static MetaDTO createDbMeta(String querySummary, String generatedSql, Integer rowCount) {
            return MetaDTO.builder()
                    .needsDb(true)
                    .querySummary(querySummary)
                    .generatedSql(generatedSql)
                    .rowCount(rowCount)
                    .build();
        }
    }
}

