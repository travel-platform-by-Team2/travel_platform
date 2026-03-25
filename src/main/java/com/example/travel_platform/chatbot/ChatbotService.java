package com.example.travel_platform.chatbot;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.weather.WeatherResponse;
import com.example.travel_platform.weather.WeatherService;

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
                },
                {
                  "name": "WEATHER",
                  "scope": "PUBLIC",
                  "supportedIntents": ["DATE_FORECAST"]
                }
              ]
            }
            """;

    private static final String CLARIFICATION_MESSAGE = "원하시는 정보를 정확히 찾지 못했어요. 기간이나 키워드를 조금 더 구체적으로 말씀해 주세요.";
    private static final String WEATHER_CLARIFICATION_MESSAGE = "날씨를 알려드리려면 지역과 날짜를 함께 말씀해 주세요. 예: 3월 28일 제주 날씨 알려줘";

    private final ChatbotLlmClient chatbotLlmClient;
    private final ChatQueryRepository chatQueryRepository;
    private final WeatherService weatherService;

    public ChatbotResponse.AskDTO ask(Integer userId, ChatbotRequest.AskDTO reqDTO) {
        String message = sanitize(reqDTO.getMessage());
        ChatbotRequest.ContextDTO context = reqDTO.getContext();
        List<ChatbotRequest.HistoryItemDTO> history = reqDTO.getHistory();
        Interpretation interpretation = interpret(message, context, history);
        logInterpretation(userId, message, interpretation);

        if (!interpretation.isDbQa()) {
            return ChatbotResponse.AskDTO.createAskResponse(
                    Mode.GENERAL_CHAT.name(),
                    answerGeneralChat(message, context, history),
                    List.of(),
                    true);
        }

        List<QueryBlock> queryBlocks = query(userId, context, interpretation);
        boolean hasSufficientData = hasSufficientData(queryBlocks);
        logQueryBlocks(userId, queryBlocks, hasSufficientData);

        if (!hasSufficientData) {
            return ChatbotResponse.AskDTO.createAskResponse(
                    Mode.DB_QA.name(),
                    resolveClarificationMessage(queryBlocks),
                    extractUsedDomains(queryBlocks),
                    false);
        }

        return ChatbotResponse.AskDTO.createAskResponse(
                Mode.DB_QA.name(),
                answerDbQa(message, context, interpretation, queryBlocks, history),
                extractUsedDomains(queryBlocks),
                true);
    }

    private Interpretation interpret(
            String message,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history) {
        try {
            return chatbotLlmClient.interpret(message, context, history, TOOL_CONTEXT);
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

    private String answerGeneralChat(
            String message,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history) {
        try {
            return chatbotLlmClient.answerGeneralChat(message, context, history);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "일반 대화 응답 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private List<QueryBlock> query(Integer userId, ChatbotRequest.ContextDTO context, Interpretation interpretation) {
        try {
            List<QueryBlock> queryBlocks = new ArrayList<>();
            for (QueryPlan queryPlan : interpretation.getQueryPlans()) {
                QueryPlan resolvedQueryPlan = resolveQueryPlan(queryPlan, interpretation, context);
                if (resolvedQueryPlan.getDomain() == Domain.WEATHER) {
                    queryBlocks.add(queryWeather(resolvedQueryPlan));
                    continue;
                }
                queryBlocks.addAll(chatQueryRepository.execute(userId, context, List.of(resolvedQueryPlan)));
            }
            return queryBlocks;
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

    private QueryBlock queryWeather(QueryPlan queryPlan) {
        String region = normalize(queryPlan.getKeyword());
        LocalDate targetDate = parseDateOrNull(queryPlan.getStartDate());

        if (region == null || targetDate == null) {
            return QueryBlock.createQueryBlock(
                    Domain.WEATHER,
                    resolveIntent(queryPlan, "DATE_FORECAST"),
                    "날씨 조회에 필요한 지역 또는 날짜가 없습니다.",
                    List.of());
        }

        try {
            WeatherResponse.WeatherDTO weather = weatherService.getWeather(region, targetDate);
            List<Map<String, Object>> items = weather.getForecasts().stream()
                    .map(this::toWeatherItem)
                    .toList();

            return QueryBlock.createQueryBlock(
                    Domain.WEATHER,
                    resolveIntent(queryPlan, "DATE_FORECAST"),
                    "날씨 조회 결과 " + items.size() + "건",
                    items);
        } catch (Exception400 e) {
            return QueryBlock.createQueryBlock(
                    Domain.WEATHER,
                    resolveIntent(queryPlan, "DATE_FORECAST"),
                    e.getMessage(),
                    List.of());
        }
    }

    private Map<String, Object> toWeatherItem(WeatherResponse.DailyForecastDTO forecast) {
        Map<String, Object> item = new HashMap<>();
        item.put("forecastDate", stringify(forecast.getForecastDate()));
        item.put("dayOfWeek", forecast.getDayOfWeek());
        item.put("weather", forecast.getWeather());
        item.put("minTemperature", forecast.getMinTemperature());
        item.put("maxTemperature", forecast.getMaxTemperature());
        item.put("rainProbability", forecast.getRainProbability());
        return item;
    }

    private String answerDbQa(
            String message,
            ChatbotRequest.ContextDTO context,
            Interpretation interpretation,
            List<QueryBlock> queryBlocks,
            List<ChatbotRequest.HistoryItemDTO> history) {
        try {
            return chatbotLlmClient.answerDbQa(message, context, history, interpretation, queryBlocks);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "DB 기반 응답 생성 중 오류가 발생했습니다.",
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

    private String resolveClarificationMessage(List<QueryBlock> queryBlocks) {
        boolean weatherOnly = !queryBlocks.isEmpty()
                && queryBlocks.stream().allMatch(block -> block.getDomain() == Domain.WEATHER);
        return weatherOnly ? WEATHER_CLARIFICATION_MESSAGE : CLARIFICATION_MESSAGE;
    }

    private List<String> extractUsedDomains(List<QueryBlock> queryBlocks) {
        return queryBlocks.stream()
                .map(block -> block.getDomain().name())
                .toList();
    }

    private QueryPlan resolveQueryPlan(
            QueryPlan queryPlan,
            Interpretation interpretation,
            ChatbotRequest.ContextDTO context) {
        if (queryPlan == null) {
            return null;
        }

        ResolvedContext resolvedContext = interpretation.getResolvedContext();
        if (resolvedContext == null || resolvedContext.getDomain() == null) {
            return applyContextFallback(queryPlan, context);
        }

        if (resolvedContext.getDomain() != queryPlan.getDomain()) {
            return applyContextFallback(queryPlan, context);
        }

        return QueryPlan.createQueryPlan(
                queryPlan.getDomain(),
                firstNonBlank(queryPlan.getIntent(), resolvedContext.getIntent()),
                firstNonBlank(queryPlan.getKeyword(), resolvedContext.getKeyword(), resolvedContext.getRegion()),
                firstNonBlank(queryPlan.getStartDate(), resolvedContext.getTargetDate()),
                firstNonBlank(queryPlan.getEndDate(), resolvedContext.getEndDate()),
                firstNonBlank(queryPlan.getCategory(), resolvedContext.getCategory()),
                firstPositive(queryPlan.getLimit(), resolvedContext.getLimit()));
    }

    private QueryPlan applyContextFallback(QueryPlan queryPlan, ChatbotRequest.ContextDTO context) {
        if (queryPlan == null || context == null || context.getTripPlanId() == null) {
            return queryPlan;
        }

        if (queryPlan.getDomain() != Domain.TRIP && queryPlan.getDomain() != Domain.CALENDAR
                && queryPlan.getDomain() != Domain.BOOKING) {
            return queryPlan;
        }

        return QueryPlan.createQueryPlan(
                queryPlan.getDomain(),
                queryPlan.getIntent(),
                firstNonBlank(queryPlan.getKeyword(), String.valueOf(context.getTripPlanId())),
                queryPlan.getStartDate(),
                queryPlan.getEndDate(),
                queryPlan.getCategory(),
                queryPlan.getLimit());
    }

    private void logInterpretation(Integer userId, String message, Interpretation interpretation) {
        log.info(
                "chatbot.interpret userId={} mode={} domains={} followUp={} missingFields={} message={}",
                userId,
                interpretation.getMode().name(),
                interpretation.getQueryPlans().stream().map(plan -> plan.getDomain().name()).toList(),
                interpretation.getResolvedContext().isFollowUp(),
                interpretation.getResolvedContext().getMissingFields(),
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

    private String resolveIntent(QueryPlan queryPlan, String defaultIntent) {
        if (queryPlan.getIntent() == null || queryPlan.getIntent().isBlank()) {
            return defaultIntent;
        }
        return queryPlan.getIntent();
    }

    private LocalDate parseDateOrNull(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private Integer firstPositive(Integer... values) {
        if (values == null) {
            return null;
        }
        for (Integer value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return null;
    }

    private String stringify(Object value) {
        return value == null ? null : value.toString();
    }

    public enum Mode {
        GENERAL_CHAT,
        DB_QA
    }

    public enum Domain {
        BOOKING,
        TRIP,
        CALENDAR,
        BOARD,
        WEATHER
    }

    @Getter
    public static class Interpretation {
        private final Mode mode;
        private final List<QueryPlan> queryPlans;
        private final ResolvedContext resolvedContext;

        private Interpretation(Mode mode, List<QueryPlan> queryPlans, ResolvedContext resolvedContext) {
            this.mode = mode == null ? Mode.GENERAL_CHAT : mode;
            this.queryPlans = queryPlans == null ? new ArrayList<>() : new ArrayList<>(queryPlans.stream()
                    .filter(plan -> plan != null && plan.getDomain() != null)
                    .limit(3)
                    .toList());
            this.resolvedContext = resolvedContext == null ? ResolvedContext.createEmptyContext() : resolvedContext;
        }

        public static Interpretation createInterpretation(
                Mode mode,
                List<QueryPlan> queryPlans,
                ResolvedContext resolvedContext) {
            return new Interpretation(mode, queryPlans, resolvedContext);
        }

        public static Interpretation createInterpretation(Mode mode, List<QueryPlan> queryPlans) {
            return createInterpretation(mode, queryPlans, ResolvedContext.createEmptyContext());
        }

        public static Interpretation createGeneralChatInterpretation() {
            return new Interpretation(Mode.GENERAL_CHAT, List.of(), ResolvedContext.createEmptyContext());
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
            this.intent = normalizeValue(intent);
            this.keyword = normalizeValue(keyword);
            this.startDate = normalizeValue(startDate);
            this.endDate = normalizeValue(endDate);
            this.category = normalizeValue(category);
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

        private static String normalizeValue(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return value.trim();
        }
    }

    @Getter
    public static class ResolvedContext {
        private final Domain domain;
        private final String intent;
        private final String region;
        private final String targetDate;
        private final String endDate;
        private final String keyword;
        private final String category;
        private final Integer tripPlanId;
        private final Integer limit;
        private final boolean followUp;
        private final List<String> missingFields;

        private ResolvedContext(
                Domain domain,
                String intent,
                String region,
                String targetDate,
                String endDate,
                String keyword,
                String category,
                Integer tripPlanId,
                Integer limit,
                boolean followUp,
                List<String> missingFields) {
            this.domain = domain;
            this.intent = normalizeValue(intent);
            this.region = normalizeValue(region);
            this.targetDate = normalizeValue(targetDate);
            this.endDate = normalizeValue(endDate);
            this.keyword = normalizeValue(keyword);
            this.category = normalizeValue(category);
            this.tripPlanId = tripPlanId;
            this.limit = limit == null || limit <= 0 ? null : Math.min(limit, 10);
            this.followUp = followUp;
            this.missingFields = missingFields == null ? List.of() : new ArrayList<>(missingFields.stream()
                    .map(ResolvedContext::normalizeValue)
                    .filter(value -> value != null)
                    .distinct()
                    .toList());
        }

        public static ResolvedContext createResolvedContext(
                Domain domain,
                String intent,
                String region,
                String targetDate,
                String endDate,
                String keyword,
                String category,
                Integer tripPlanId,
                Integer limit,
                boolean followUp,
                List<String> missingFields) {
            return new ResolvedContext(
                    domain,
                    intent,
                    region,
                    targetDate,
                    endDate,
                    keyword,
                    category,
                    tripPlanId,
                    limit,
                    followUp,
                    missingFields);
        }

        public static ResolvedContext createEmptyContext() {
            return new ResolvedContext(null, null, null, null, null, null, null, null, null, false, List.of());
        }

        private static String normalizeValue(String value) {
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
