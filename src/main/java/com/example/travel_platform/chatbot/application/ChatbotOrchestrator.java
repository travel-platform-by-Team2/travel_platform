package com.example.travel_platform.chatbot.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmSearchReview;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

import lombok.RequiredArgsConstructor;

/**
 * 챗봇 질문 처리 메인 서비스.
 *
 * 처리 흐름:
 * 1) 사용자 질문 정규화
 * 2) LLM 1차 계획 수립 (needsDb, queryIntent, querySummary, sql, answer)
 * 3) DB 필요 여부 분기
 *    - false: LLM이 생성한 direct answer 반환
 *    - true: SQL 실행 -> LLM 재평가 반복(최대 5회) -> 최종 답변 생성
 */
@Service
@RequiredArgsConstructor
public class ChatbotOrchestrator {

    private static final int MAX_DB_SEARCH_ATTEMPTS = 5;
    private static final String DEFAULT_QUERY_INTENT = "GENERAL_CHAT";
    private static final String PROCESSING_TYPE_DIRECT_LLM = "DIRECT_LLM";
    private static final String PROCESSING_TYPE_DB_QUERY = "DB_QUERY";

    private final ChatbotPlanService chatbotPlanService;
    private final ChatbotQueryService chatbotQueryService;
    private final ChatbotAnswerService chatbotAnswerService;

    /**
     * 질문 1건을 처리한다.
     *
     * @param reqDTO 사용자 질문 요청
     * @return 처리 결과(DIRECT_LLM 또는 DB_QUERY)
     */
    public ChatbotResponse.AskDTO ask(ChatbotRequest.AskDTO reqDTO) {
        String message = sanitize(reqDTO.getMessage());
        ChatbotRequest.ContextDTO context = reqDTO.getContext();
        ChatbotLlmPlan llmPlan = chatbotPlanService.createPlan(message, context);

        if (!llmPlan.needsDb()) {
            return buildDirectResponse(llmPlan);
        }

        return buildDbResponse(message, context, llmPlan);
    }

    /**
     * 질문 문자열을 정규화한다.
     * null 입력을 방어하고 좌우 공백을 제거한다.
     */
    private String sanitize(String message) {
        return message == null ? "" : message.trim();
    }

    private ChatbotResponse.AskDTO buildDirectResponse(ChatbotLlmPlan llmPlan) {
        return ChatbotResponse.AskDTO.createAskResponse(
                PROCESSING_TYPE_DIRECT_LLM,
                chatbotAnswerService.resolveDirectAnswer(llmPlan),
                ChatbotResponse.MetaDTO.createDirectMeta());
    }

    private ChatbotResponse.AskDTO buildDbResponse(
            String message,
            ChatbotRequest.ContextDTO context,
            ChatbotLlmPlan initialPlan) {
        DbSearchResult dbSearchResult = resolveDbAnswer(message, context, initialPlan);
        ChatbotQueryService.QueryResult queryResult = dbSearchResult.queryResult();

        return ChatbotResponse.AskDTO.createAskResponse(
                PROCESSING_TYPE_DB_QUERY,
                dbSearchResult.answer(),
                ChatbotResponse.MetaDTO.createDbMeta(
                        queryResult.querySummary(),
                        queryResult.sql(),
                        queryResult.rows().size()));
    }

    private DbSearchResult resolveDbAnswer(
            String message,
            ChatbotRequest.ContextDTO context,
            ChatbotLlmPlan initialPlan) {
        DbSearchState searchState = createSearchState(initialPlan);
        List<ChatbotSearchAttempt> searchAttempts = new ArrayList<>();
        ChatbotQueryService.QueryResult lastQueryResult = null;

        for (int attempt = 1; attempt <= MAX_DB_SEARCH_ATTEMPTS; attempt++) {
            lastQueryResult = executeSearch(searchState);
            searchAttempts.add(createSearchAttempt(attempt, searchState.queryIntent(), lastQueryResult));

            ChatbotLlmSearchReview review = chatbotPlanService.reviewSearch(
                    message,
                    context,
                    searchState.queryIntent(),
                    searchAttempts,
                    MAX_DB_SEARCH_ATTEMPTS);
            updateLastAttemptReason(searchAttempts, review.decisionReason());

            if (!review.shouldContinue()) {
                String answer = createDbAnswer(message, searchState.queryIntent(), searchAttempts, false);
                return new DbSearchResult(answer, lastQueryResult);
            }

            searchState = nextSearchState(searchState, review, lastQueryResult);
        }

        String answer = createDbAnswer(message, searchState.queryIntent(), searchAttempts, true);
        return new DbSearchResult(answer, lastQueryResult);
    }

    private DbSearchState createSearchState(ChatbotLlmPlan initialPlan) {
        return new DbSearchState(
                toTextOrDefault(initialPlan.queryIntent(), DEFAULT_QUERY_INTENT),
                initialPlan.querySummary(),
                requireSql(initialPlan.sql(), "LLM 계획에 DB 조회 SQL이 없습니다."));
    }

    private ChatbotQueryService.QueryResult executeSearch(DbSearchState searchState) {
        return chatbotQueryService.execute(searchState.sql(), searchState.querySummary());
    }

    private ChatbotSearchAttempt createSearchAttempt(
            int attempt,
            String queryIntent,
            ChatbotQueryService.QueryResult queryResult) {
        return new ChatbotSearchAttempt(
                attempt,
                queryIntent,
                queryResult.querySummary(),
                queryResult.sql(),
                queryResult.rows(),
                "");
    }

    private DbSearchState nextSearchState(
            DbSearchState currentState,
            ChatbotLlmSearchReview review,
            ChatbotQueryService.QueryResult lastQueryResult) {
        return new DbSearchState(
                toTextOrDefault(review.queryIntent(), currentState.queryIntent()),
                toTextOrDefault(review.querySummary(), lastQueryResult.querySummary()),
                requireSql(review.sql(), "LLM 재탐색 계획에 DB 조회 SQL이 없습니다."));
    }

    private void updateLastAttemptReason(List<ChatbotSearchAttempt> searchAttempts, String decisionReason) {
        int lastIndex = searchAttempts.size() - 1;
        ChatbotSearchAttempt updatedAttempt = searchAttempts.get(lastIndex)
                .withEvaluationReason(toTextOrDefault(decisionReason, ""));
        searchAttempts.set(lastIndex, updatedAttempt);
    }

    private String createDbAnswer(
            String message,
            String queryIntent,
            List<ChatbotSearchAttempt> searchAttempts,
            boolean exhausted) {
        return chatbotAnswerService.createDbAnswer(
                message,
                queryIntent,
                searchAttempts,
                exhausted);
    }

    private String requireSql(String sql, String errorMessage) {
        if (sql == null || sql.isBlank()) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    errorMessage,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return sql;
    }

    private String toTextOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private record DbSearchState(String queryIntent, String querySummary, String sql) {
    }

    private record DbSearchResult(String answer, ChatbotQueryService.QueryResult queryResult) {
    }
}

