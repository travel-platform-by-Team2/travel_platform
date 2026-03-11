package com.example.travel_platform.chatbot.infra.llm;

import java.util.List;
import java.util.Map;

/**
 * 챗봇 DB 탐색 시도 1건의 이력.
 *
 * @param attempt 시도 번호(1부터 시작)
 * @param queryIntent 질의 의도 식별자
 * @param querySummary SQL 목적 요약
 * @param sql 실제 실행된 SQL
 * @param rows 조회 결과 row 목록
 * @param evaluationReason 다음 탐색 또는 종료 판단 이유
 */
public record ChatbotSearchAttempt(
        int attempt,
        String queryIntent,
        String querySummary,
        String sql,
        List<Map<String, Object>> rows,
        String evaluationReason) {

    public ChatbotSearchAttempt withEvaluationReason(String nextEvaluationReason) {
        return new ChatbotSearchAttempt(
                attempt,
                queryIntent,
                querySummary,
                sql,
                rows,
                nextEvaluationReason == null ? "" : nextEvaluationReason);
    }
}
