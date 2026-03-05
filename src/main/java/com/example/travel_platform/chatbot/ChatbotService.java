package com.example.travel_platform.chatbot;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.travel_platform.chatbot.exception.ChatbotException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final String INTENT_BOOKING_LIST = "USER_BOOKING_LIST";
    private static final String INTENT_CALENDAR_LIST = "USER_CALENDAR_LIST";
    private static final String INTENT_BOARD_POST_INFO = "BOARD_POST_INFO";
    private static final String INTENT_TRIP_PLAN_INFO = "TRIP_PLAN_INFO";
    private static final String INTENT_GENERAL_CHAT = "GENERAL_CHAT";

    private final JdbcTemplate jdbcTemplate;

    public ChatbotResponse.AskDTO ask(ChatbotRequest.AskDTO reqDTO) {
        String message = sanitize(reqDTO.getMessage());
        ClassificationResult classification = classifyQuestion(message);

        if (!classification.needsDb()) {
            return ChatbotResponse.AskDTO.builder()
                    .processingType("DIRECT_LLM")
                    .answer(buildDirectAnswer(message))
                    .meta(ChatbotResponse.MetaDTO.builder()
                            .needsDb(false)
                            .build())
                    .build();
        }

        SqlPlan sqlPlan = generateSqlPlan(classification, reqDTO.getContext());
        List<Map<String, Object>> queryRows = executeQuery(sqlPlan);
        String answer = buildDbAnswer(classification.queryIntent(), queryRows);

        return ChatbotResponse.AskDTO.builder()
                .processingType("DB_QUERY")
                .answer(answer)
                .meta(ChatbotResponse.MetaDTO.builder()
                        .needsDb(true)
                        .querySummary(sqlPlan.summary())
                        .generatedSql(sqlPlan.sql())
                        .rowCount(queryRows.size())
                        .build())
                .build();
    }

    private String sanitize(String message) {
        return message == null ? "" : message.trim();
    }

    private ClassificationResult classifyQuestion(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);

        if (containsAny(normalized, "\uC608\uC57D", "booking", "\uC219\uC18C", "hotel")) {
            return new ClassificationResult(true, "Booking data lookup is required.", INTENT_BOOKING_LIST);
        }
        if (containsAny(normalized, "\uC77C\uC815", "calendar", "\uC2A4\uCF00\uC904", "schedule")) {
            return new ClassificationResult(true, "Calendar event lookup is required.", INTENT_CALENDAR_LIST);
        }
        if (containsAny(normalized, "\uAC8C\uC2DC\uAE00", "\uAC8C\uC2DC\uD310", "\uCEE4\uBBA4\uB2C8\uD2F0", "\uB313\uAE00", "board", "community")) {
            return new ClassificationResult(true, "Board data lookup is required.", INTENT_BOARD_POST_INFO);
        }
        if (containsAny(normalized, "\uC5EC\uD589", "\uCF54\uC2A4", "trip", "\uD50C\uB79C", "\uACC4\uD68D")) {
            return new ClassificationResult(true, "Trip plan data lookup is required.", INTENT_TRIP_PLAN_INFO);
        }

        return new ClassificationResult(false, "General question that does not require DB lookup.", INTENT_GENERAL_CHAT);
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private SqlPlan generateSqlPlan(ClassificationResult classification, ChatbotRequest.ContextDTO context) {
        return switch (classification.queryIntent()) {
            case INTENT_BOOKING_LIST -> new SqlPlan(
                    """
                            select b.id, b.lodging_name, b.check_in, b.check_out, b.guest_count, b.total_price
                            from booking_tb b
                            order by b.check_in desc, b.id desc
                            limit 5
                            """,
                    "Fetch recent booking list",
                    new Object[0]);
            case INTENT_CALENDAR_LIST -> new SqlPlan(
                    """
                            select c.id, c.title, c.start_at, c.end_at, c.event_type
                            from calendar_event_tb c
                            order by c.start_at asc, c.id asc
                            limit 5
                            """,
                    "Fetch upcoming calendar events",
                    new Object[0]);
            case INTENT_BOARD_POST_INFO -> new SqlPlan(
                    """
                            select p.id, p.title, p.view_count, p.created_at
                            from board_tb p
                            order by p.created_at desc, p.id desc
                            limit 5
                            """,
                    "Fetch latest board posts",
                    new Object[0]);
            case INTENT_TRIP_PLAN_INFO -> buildTripPlanSqlPlan(context);
            default -> throw new ChatbotException(
                    "CHATBOT_INTERNAL_ERROR",
                    "Failed to process classification result.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    private SqlPlan buildTripPlanSqlPlan(ChatbotRequest.ContextDTO context) {
        Integer tripPlanId = context == null ? null : context.getTripPlanId();
        if (tripPlanId != null) {
            return new SqlPlan(
                    """
                            select tp.id, tp.title, tp.start_date, tp.end_date, count(tpl.id) as place_count
                            from trip_plan_tb tp
                            left join trip_place_tb tpl on tpl.trip_plan_id = tp.id
                            where tp.id = ?
                            group by tp.id, tp.title, tp.start_date, tp.end_date
                            """,
                    "Fetch requested trip plan detail",
                    new Object[] { tripPlanId });
        }

        return new SqlPlan(
                """
                        select tp.id, tp.title, tp.start_date, tp.end_date, count(tpl.id) as place_count
                        from trip_plan_tb tp
                        left join trip_place_tb tpl on tpl.trip_plan_id = tp.id
                        group by tp.id, tp.title, tp.start_date, tp.end_date
                        order by tp.start_date desc, tp.id desc
                        limit 5
                        """,
                "Fetch recent trip plans",
                new Object[0]);
    }

    private List<Map<String, Object>> executeQuery(SqlPlan sqlPlan) {
        try {
            if (sqlPlan.params().length == 0) {
                return jdbcTemplate.queryForList(sqlPlan.sql());
            }
            return jdbcTemplate.queryForList(sqlPlan.sql(), sqlPlan.params());
        } catch (Exception e) {
            throw new ChatbotException(
                    "CHATBOT_INTERNAL_ERROR",
                    "Error occurred while querying DB.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private String buildDirectAnswer(String message) {
        return "This question was classified as answerable without DB lookup. Development mode answer: " + message;
    }

    private String buildDbAnswer(String queryIntent, List<Map<String, Object>> queryRows) {
        if (queryRows.isEmpty()) {
            return "No data was found for the requested condition.";
        }

        Map<String, Object> firstRow = queryRows.get(0);
        return switch (queryIntent) {
            case INTENT_BOOKING_LIST -> String.format(
                    "Found %d booking records. The most recent lodging is '%s'.",
                    queryRows.size(),
                    toText(firstRow.get("lodging_name")));
            case INTENT_CALENDAR_LIST -> String.format(
                    "Found %d calendar events. The earliest event is '%s'.",
                    queryRows.size(),
                    toText(firstRow.get("title")));
            case INTENT_BOARD_POST_INFO -> String.format(
                    "Found %d board posts. The latest post title is '%s'.",
                    queryRows.size(),
                    toText(firstRow.get("title")));
            case INTENT_TRIP_PLAN_INFO -> String.format(
                    "Found %d trip plans. The first plan is '%s' with %s places.",
                    queryRows.size(),
                    toText(firstRow.get("title")),
                    toText(firstRow.get("place_count")));
            default -> String.format("Found %d rows from DB query.", queryRows.size());
        };
    }

    private String toText(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private record ClassificationResult(boolean needsDb, String reason, String queryIntent) {
    }

    private record SqlPlan(String sql, String summary, Object[] params) {
    }
}
