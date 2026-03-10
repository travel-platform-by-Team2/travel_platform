package com.example.travel_platform.chatbot.infra.llm;

/**
 * DB 탐색 결과를 바탕으로 다음 행동을 결정하는 LLM 판단 결과.
 *
 * @param shouldContinue DB를 다시 조회해야 하는지 여부
 * @param queryIntent 다음 탐색 또는 최종 답변에 사용할 질의 의도
 * @param querySummary 다음 SQL 목적 요약
 * @param sql 다음 탐색 SQL
 * @param decisionReason 탐색 지속 또는 종료 판단 이유
 */
public record ChatbotLlmSearchReview(
        boolean shouldContinue,
        String queryIntent,
        String querySummary,
        String sql,
        String decisionReason) {
}
