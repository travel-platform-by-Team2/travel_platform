package com.example.travel_platform.chatbot.application;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotQueryService {

    private static final String DEFAULT_QUERY_SUMMARY = "LLM 생성 SQL 조회";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 50;

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "booking_tb",
            "calendar_event_tb",
            "board_tb",
            "trip_plan_tb",
            "trip_place_tb");

    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "insert",
            "update",
            "delete",
            "merge",
            "upsert",
            "replace",
            "drop",
            "truncate",
            "alter",
            "create",
            "grant",
            "revoke",
            "call",
            "execute",
            "exec");

    private static final Pattern LIMIT_PATTERN = Pattern.compile("(?i)\\blimit\\s+(\\d+)\\b");
    private static final Pattern TABLE_REF_PATTERN = Pattern.compile("(?i)\\b(?:from|join)\\s+([`\"\\[]?[a-zA-Z0-9_.]+[`\"\\]]?)");

    private final JdbcTemplate jdbcTemplate;

    public QueryResult execute(ChatbotLlmPlan llmPlan) {
        return execute(llmPlan.sql(), llmPlan.querySummary());
    }

    public QueryResult execute(String sql, String querySummary) {
        if (sql == null || sql.isBlank()) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM 계획에 DB 조회 SQL이 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String safeSql = ensureSafeSql(sql);
        String safeQuerySummary = toTextOrDefault(querySummary, DEFAULT_QUERY_SUMMARY);
        List<Map<String, Object>> rows = executeQuery(safeSql);
        return new QueryResult(safeSql, safeQuerySummary, rows);
    }

    private List<Map<String, Object>> executeQuery(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "DB 조회 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    private String ensureSafeSql(String rawSql) {
        String sql = normalizeSql(rawSql);

        if (!sql.toLowerCase(Locale.ROOT).startsWith("select ")) {
            throw unsafeSql("SELECT 조회문만 허용됩니다.");
        }
        if (sql.contains(";")) {
            throw unsafeSql("다중 SQL 문은 허용되지 않습니다.");
        }
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/")) {
            throw unsafeSql("SQL 주석은 허용되지 않습니다.");
        }

        validateBlockedKeyword(sql);
        validateAllowedTables(sql);
        return enforceLimit(sql);
    }

    private String normalizeSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.trim().replaceAll("\\s+", " ");
    }

    private void validateBlockedKeyword(String sql) {
        String lowerSql = sql.toLowerCase(Locale.ROOT);
        for (String keyword : BLOCKED_KEYWORDS) {
            if (containsWord(lowerSql, keyword)) {
                throw unsafeSql("허용되지 않은 SQL 키워드가 포함되어 있습니다.");
            }
        }
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    private void validateAllowedTables(String sql) {
        Matcher matcher = TABLE_REF_PATTERN.matcher(sql);
        Set<String> referencedTables = new HashSet<>();
        while (matcher.find()) {
            String rawTable = matcher.group(1);
            if (rawTable == null || rawTable.isBlank()) {
                continue;
            }
            String table = normalizeTableName(rawTable);
            if (!table.isBlank()) {
                referencedTables.add(table);
            }
        }

        if (referencedTables.isEmpty()) {
            throw unsafeSql("허용된 테이블 참조가 없는 SQL입니다.");
        }

        boolean hasDisallowedTable = referencedTables.stream()
                .anyMatch(table -> !ALLOWED_TABLES.contains(table));
        if (hasDisallowedTable) {
            throw unsafeSql("허용되지 않은 테이블 참조가 포함되어 있습니다.");
        }
    }

    private String normalizeTableName(String rawTable) {
        String table = rawTable.trim()
                .replace("`", "")
                .replace("\"", "")
                .replace("[", "")
                .replace("]", "");
        String[] split = table.split("\\.");
        return split[split.length - 1].toLowerCase(Locale.ROOT);
    }

    private String enforceLimit(String sql) {
        Matcher matcher = LIMIT_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return sql + " limit " + DEFAULT_LIMIT;
        }

        int parsedLimit;
        try {
            parsedLimit = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return matcher.replaceFirst("limit " + DEFAULT_LIMIT);
        }

        if (parsedLimit <= 0) {
            return matcher.replaceFirst("limit " + DEFAULT_LIMIT);
        }
        if (parsedLimit > MAX_LIMIT) {
            return matcher.replaceFirst("limit " + MAX_LIMIT);
        }
        return sql;
    }

    private ApiException unsafeSql(String message) {
        return new ApiException(
                "CHATBOT_UNSAFE_SQL",
                message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String toTextOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public record QueryResult(String sql, String querySummary, List<Map<String, Object>> rows) {
    }
}
