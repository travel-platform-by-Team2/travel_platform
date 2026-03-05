package com.example.travel_platform.chatbot;

import lombok.Builder;
import lombok.Data;

// 챗봇 응답 포맷 DTO를 정의하는 클래스
public class ChatbotResponse {

    @Data
    @Builder
    public static class AskDTO {
        private String processingType;
        private String answer;
        private MetaDTO meta;
    }

    @Data
    @Builder
    public static class MetaDTO {
        private Boolean needsDb;
        private String querySummary;
        private String generatedSql;
        private Integer rowCount;
    }
}
