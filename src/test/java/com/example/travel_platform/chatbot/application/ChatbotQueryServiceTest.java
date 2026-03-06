package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;

import static org.mockito.Mockito.mock;

class ChatbotQueryServiceTest {

    @Test
    void execute_appendsDefaultLimit_whenMissingLimit() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "BOARD_LIST",
                "게시글 조회",
                "select id, title from board_tb order by id desc",
                "");
        String expectedSql = "select id, title from board_tb order by id desc limit 5";
        List<Map<String, Object>> rows = List.of(Map.of("id", 1, "title", "t1"));
        when(jdbcTemplate.queryForList(expectedSql)).thenReturn(rows);

        ChatbotQueryService.QueryResult result = service.execute(plan);

        assertEquals(expectedSql, result.sql());
        assertEquals(1, result.rows().size());
        verify(jdbcTemplate).queryForList(expectedSql);
    }

    @Test
    void execute_clampsLimit_whenTooLarge() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "BOARD_LIST",
                "게시글 조회",
                "select id from board_tb limit 999",
                "");
        String expectedSql = "select id from board_tb limit 50";
        when(jdbcTemplate.queryForList(expectedSql)).thenReturn(List.of());

        ChatbotQueryService.QueryResult result = service.execute(plan);

        assertEquals(expectedSql, result.sql());
        verify(jdbcTemplate).queryForList(expectedSql);
    }

    @Test
    void execute_rejectsNonSelectSql() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "MALICIOUS",
                "",
                "delete from board_tb",
                "");

        ApiException exception = assertThrows(ApiException.class, () -> service.execute(plan));

        assertEquals("CHATBOT_UNSAFE_SQL", exception.getCode());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void execute_rejectsMultipleStatements() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "MALICIOUS",
                "",
                "select id from board_tb; select * from trip_plan_tb",
                "");

        ApiException exception = assertThrows(ApiException.class, () -> service.execute(plan));

        assertEquals("CHATBOT_UNSAFE_SQL", exception.getCode());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void execute_rejectsDisallowedTable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "USER_INFO",
                "",
                "select id, email from user_tb",
                "");

        ApiException exception = assertThrows(ApiException.class, () -> service.execute(plan));

        assertEquals("CHATBOT_UNSAFE_SQL", exception.getCode());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void execute_allowsJoinOnAllowedTables() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotQueryService service = new ChatbotQueryService(jdbcTemplate);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "TRIP_DETAIL",
                "",
                "select p.id from trip_plan_tb p join trip_place_tb pp on pp.trip_plan_id = p.id limit 10",
                "");
        String expectedSql = "select p.id from trip_plan_tb p join trip_place_tb pp on pp.trip_plan_id = p.id limit 10";
        when(jdbcTemplate.queryForList(expectedSql)).thenReturn(List.of(Map.of("id", 1)));

        ChatbotQueryService.QueryResult result = service.execute(plan);

        assertEquals(expectedSql, result.sql());
        verify(jdbcTemplate).queryForList(expectedSql);
    }
}
