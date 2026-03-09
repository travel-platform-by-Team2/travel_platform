package com.example.travel_platform.chatbot.infra.llm;

/**
 * LLM 1차 계획 결과 모델.
 *
 * @param needsDb DB 조회 필요 여부
 * @param queryIntent 질의 의도 식별자
 * @param querySummary SQL 목적 요약
 * @param sql DB 조회 SQL(필요 시)
 * @param answer DB 불필요 시 즉시 반환 가능한 최종 답변
 */
public record ChatbotLlmPlan(
        boolean needsDb,
        String queryIntent,
        String querySummary,
        String sql,
        String answer) {
}
