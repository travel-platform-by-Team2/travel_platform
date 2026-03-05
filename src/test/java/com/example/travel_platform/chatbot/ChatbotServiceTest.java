package com.example.travel_platform.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.travel_platform.chatbot.exception.ChatbotException;

class ChatbotServiceTest {

    @Test
    void ask_directQuestion_returnsDirectLlmWithoutDbQuery() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotService chatbotService = new ChatbotService(jdbcTemplate);

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("hello");

        ChatbotResponse.AskDTO response = chatbotService.ask(request);

        assertEquals("DIRECT_LLM", response.getProcessingType());
        assertNotNull(response.getMeta());
        assertEquals(false, response.getMeta().getNeedsDb());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void ask_bookingQuestion_executesQueryAndReturnsDbMeta() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotService chatbotService = new ChatbotService(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(Map.of("lodging_name", "Ocean View Hotel")));

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("booking list");

        ChatbotResponse.AskDTO response = chatbotService.ask(request);

        assertEquals("DB_QUERY", response.getProcessingType());
        assertNotNull(response.getMeta());
        assertEquals(true, response.getMeta().getNeedsDb());
        assertEquals(1, response.getMeta().getRowCount());
        assertNotNull(response.getMeta().getQuerySummary());
        assertNotNull(response.getMeta().getGeneratedSql());
        verify(jdbcTemplate).queryForList(anyString());
    }

    @Test
    void ask_dbFailure_throwsChatbotException() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ChatbotService chatbotService = new ChatbotService(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("db error"));

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("booking status");

        ChatbotException exception = assertThrows(ChatbotException.class, () -> chatbotService.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
    }
}
