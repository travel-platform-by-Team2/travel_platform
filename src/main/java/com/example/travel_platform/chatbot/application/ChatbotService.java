package com.example.travel_platform.chatbot.application;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private static final String TOOL_CONTEXT = """
            {
              "rules": {
                "loginRequired": true,
                "queryLimit": 3,
                "doNotGenerateSql": true
              },
              "domains": [
                {
                  "name": "BOOKING",
                  "scope": "PRIVATE",
                  "supportedIntents": ["UPCOMING_LIST", "RECENT_LIST", "PERIOD_LIST", "KEYWORD_SEARCH", "TRIP_PLAN_BOOKINGS"]
                },
                {
                  "name": "TRIP",
                  "scope": "PRIVATE",
                  "supportedIntents": ["UPCOMING_LIST", "RECENT_LIST", "PERIOD_LIST", "KEYWORD_SEARCH", "CURRENT_TRIP"]
                },
                {
                  "name": "CALENDAR",
                  "scope": "PRIVATE",
                  "supportedIntents": ["MONTH_LIST", "PERIOD_LIST", "KEYWORD_SEARCH", "TRIP_PLAN_EVENTS"]
                },
                {
                  "name": "BOARD",
                  "scope": "PUBLIC",
                  "supportedIntents": ["RECENT_LIST", "KEYWORD_SEARCH", "CATEGORY_SEARCH"]
                }
              ]
            }
            """;

    private static final String CLARIFICATION_MESSAGE = "원하시는 정보를 정확히 찾지 못했어요. 기간이나 키워드를 조금 더 구체적으로 말씀해 주세요.";

    private final ChatbotLlmClient chatbotLlmClient;
    private final ChatQueryRepository chatQueryRepository;

    public ChatbotResponse.AskDTO ask(Integer userId, ChatbotRequest.AskDTO reqDTO) {
        String message = sanitize(reqDTO.getMessage());
        ChatbotRequest.ContextDTO context = reqDTO.getContext();
        Interpretation interpretation = interpret(message, context);
        logInterpretation(userId, message, interpretation);

        if (!interpretation.isDbQa()) {
            return ChatbotResponse.AskDTO.createAskResponse(
                    Mode.GENERAL_CHAT.name(),
                    answerGeneralChat(message, context),
                    List.of(),
                    true);
        }

        List<QueryBlock> queryBlocks = query(userId, context, interpretation);
        boolean hasSufficientData = hasSufficientData(queryBlocks);
        logQueryBlocks(userId, queryBlocks, hasSufficientData);

        if (!hasSufficientData) {
            return ChatbotResponse.AskDTO.createAskResponse(
                    Mode.DB_QA.name(),
                    CLARIFICATION_MESSAGE,
                    extractUsedDomains(queryBlocks),
                    false);
        }

        return ChatbotResponse.AskDTO.createAskResponse(
                Mode.DB_QA.name(),
                answerDbQa(message, context, interpretation, queryBlocks),
                extractUsedDomains(queryBlocks),
                true);
    }

    private Interpretation interpret(String message, ChatbotRequest.ContextDTO context) {
        try {
            return chatbotLlmClient.interpret(message, context, TOOL_CONTEXT);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "챗봇 질문 해석 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private String answerGeneralChat(String message, ChatbotRequest.ContextDTO context) {
        try {
            return chatbotLlmClient.answerGeneralChat(message, context);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "일반 대화 답변 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private List<QueryBlock> query(Integer userId, ChatbotRequest.ContextDTO context, Interpretation interpretation) {
        try {
            return chatQueryRepository.execute(userId, context, interpretation.getQueryPlans());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "챗봇 조회 처리 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private String answerDbQa(
            String message,
            ChatbotRequest.ContextDTO context,
            Interpretation interpretation,
            List<QueryBlock> queryBlocks) {
        try {
            return chatbotLlmClient.answerDbQa(message, context, interpretation, queryBlocks);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "DB 기반 답변 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private boolean hasSufficientData(List<QueryBlock> queryBlocks) {
        if (queryBlocks.isEmpty()) {
            return false;
        }
        return queryBlocks.stream().allMatch(QueryBlock::isHasData);
    }

    private List<String> extractUsedDomains(List<QueryBlock> queryBlocks) {
        return queryBlocks.stream()
                .map(block -> block.getDomain().name())
                .toList();
    }

    private void logInterpretation(Integer userId, String message, Interpretation interpretation) {
        log.info(
                "chatbot.interpret userId={} mode={} domains={} message={}",
                userId,
                interpretation.getMode().name(),
                interpretation.getQueryPlans().stream().map(plan -> plan.getDomain().name()).toList(),
                message);
    }

    private void logQueryBlocks(Integer userId, List<QueryBlock> queryBlocks, boolean hasSufficientData) {
        log.info(
                "chatbot.query userId={} hasSufficientData={} results={}",
                userId,
                hasSufficientData,
                queryBlocks.stream()
                        .map(block -> block.getDomain().name() + ":" + block.getItemCount())
                        .toList());
    }

    private String sanitize(String message) {
        if (message == null) {
            return "";
        }
        return message.trim();
    }

    public enum Mode {
        GENERAL_CHAT,
        DB_QA
    }

    public enum Domain {
        BOOKING,
        TRIP,
        CALENDAR,
        BOARD
    }

    @Getter
    public static class Interpretation {
        private final Mode mode;
        private final List<QueryPlan> queryPlans;

        private Interpretation(Mode mode, List<QueryPlan> queryPlans) {
            this.mode = mode == null ? Mode.GENERAL_CHAT : mode;
            this.queryPlans = queryPlans == null ? new ArrayList<>() : new ArrayList<>(queryPlans.stream()
                    .filter(plan -> plan != null && plan.getDomain() != null)
                    .limit(3)
                    .toList());
        }

        public static Interpretation createInterpretation(Mode mode, List<QueryPlan> queryPlans) {
            return new Interpretation(mode, queryPlans);
        }

        public static Interpretation createGeneralChatInterpretation() {
            return new Interpretation(Mode.GENERAL_CHAT, List.of());
        }

        public boolean isDbQa() {
            return mode == Mode.DB_QA;
        }
    }

    @Getter
    public static class QueryPlan {
        private final Domain domain;
        private final String intent;
        private final String keyword;
        private final String startDate;
        private final String endDate;
        private final String category;
        private final Integer limit;

        private QueryPlan(
                Domain domain,
                String intent,
                String keyword,
                String startDate,
                String endDate,
                String category,
                Integer limit) {
            this.domain = domain;
            this.intent = normalize(intent);
            this.keyword = normalize(keyword);
            this.startDate = normalize(startDate);
            this.endDate = normalize(endDate);
            this.category = normalize(category);
            this.limit = limit == null || limit <= 0 ? 5 : Math.min(limit, 10);
        }

        public static QueryPlan createQueryPlan(
                Domain domain,
                String intent,
                String keyword,
                String startDate,
                String endDate,
                String category,
                Integer limit) {
            return new QueryPlan(domain, intent, keyword, startDate, endDate, category, limit);
        }

        private static String normalize(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return value.trim();
        }
    }

    @Getter
    public static class QueryBlock {
        private final Domain domain;
        private final String intent;
        private final String summary;
        private final Integer itemCount;
        private final boolean hasData;
        private final List<?> items;

        private QueryBlock(
                Domain domain,
                String intent,
                String summary,
                Integer itemCount,
                boolean hasData,
                List<?> items) {
            this.domain = domain;
            this.intent = intent;
            this.summary = summary;
            this.itemCount = itemCount == null ? 0 : itemCount;
            this.hasData = hasData;
            this.items = items == null ? List.of() : new ArrayList<>(items);
        }

        public static QueryBlock createQueryBlock(
                Domain domain,
                String intent,
                String summary,
                List<?> items) {
            List<?> safeItems = items == null ? List.of() : items;
            return new QueryBlock(domain, intent, summary, safeItems.size(), !safeItems.isEmpty(), safeItems);
        }
    }
}
