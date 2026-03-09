package com.example.travel_platform.chatbot.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 챗봇 응답 DTO 모음.
 */
public class ChatbotResponse {

    /**
     * 질문 1건 처리 결과.
     */
    @Data
    @Builder
    public static class AskDTO {
        /** 처리 방식: DIRECT_LLM 또는 DB_QUERY */
        private String processingType;
        /** 사용자에게 보여줄 최종 자연어 응답 */
        private String answer;
        /** 처리 메타 정보 */
        private MetaDTO meta;
    }

    /**
     * 처리 메타 정보.
     * DB 분기일 때 추적용 정보가 추가로 채워진다.
     */
    @Data
    @Builder
    public static class MetaDTO {
        /** DB 조회 수행 여부 */
        private Boolean needsDb;
        /** SQL 생성 목적 요약(선택) */
        private String querySummary;
        /** 실제 실행 SQL(선택) */
        private String generatedSql;
        /** 조회 결과 row 수(선택) */
        private Integer rowCount;
    }
}
