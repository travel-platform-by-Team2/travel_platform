package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;

class ChatbotOrchestratorTest {

    @Test
    void ask_directQuestion_returnsDirectLlmWithoutDbQuery() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(false, "GENERAL_CHAT", "", "", "llm answer");
        when(chatbotPlanService.createPlan(eq("hello"), any())).thenReturn(plan);
        when(chatbotAnswerService.resolveDirectAnswer(plan)).thenReturn("llm answer");

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("hello");

        ChatbotResponse.AskDTO response = chatbotOrchestrator.ask(request);

        assertEquals("DIRECT_LLM", response.getProcessingType());
        assertEquals("llm answer", response.getAnswer());
        assertNotNull(response.getMeta());
        assertEquals(false, response.getMeta().getNeedsDb());
        verify(chatbotPlanService).createPlan(eq("hello"), any());
        verify(chatbotAnswerService).resolveDirectAnswer(plan);
        verifyNoInteractions(chatbotQueryService);
    }

    @Test
    void ask_bookingQuestion_executesQueryAndReturnsDbMeta() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        String sql = "select id, lodging_name from booking_tb order by id desc limit 5";
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "USER_BOOKING_LIST",
                "Fetch booking list",
                sql,
                "");
        List<Map<String, Object>> rows = List.of(Map.of("lodging_name", "Ocean View Hotel"));
        when(chatbotPlanService.createPlan(eq("booking list"), any())).thenReturn(plan);
        when(chatbotQueryService.execute(plan)).thenReturn(new ChatbotQueryService.QueryResult(sql, "Fetch booking list", rows));
        when(chatbotAnswerService.createDbAnswer("booking list", plan, rows))
                .thenReturn("db answer");

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("booking list");

        ChatbotResponse.AskDTO response = chatbotOrchestrator.ask(request);

        assertEquals("DB_QUERY", response.getProcessingType());
        assertNotNull(response.getMeta());
        assertEquals(true, response.getMeta().getNeedsDb());
        assertEquals(1, response.getMeta().getRowCount());
        assertEquals("Fetch booking list", response.getMeta().getQuerySummary());
        assertEquals(sql, response.getMeta().getGeneratedSql());
        assertEquals("db answer", response.getAnswer());
        verify(chatbotQueryService).execute(plan);
        verify(chatbotAnswerService).createDbAnswer("booking list", plan, rows);
    }

    @Test
    void ask_queryFailure_throwsChatbotException() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        String sql = "select id from booking_tb";
        ChatbotLlmPlan plan = new ChatbotLlmPlan(true, "USER_BOOKING_LIST", "Fetch booking list", sql, "");
        when(chatbotPlanService.createPlan(eq("booking status"), any())).thenReturn(plan);
        when(chatbotQueryService.execute(plan))
                .thenThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "db error", HttpStatus.INTERNAL_SERVER_ERROR));

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("booking status");

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("booking status"), any());
    }

    @Test
    void ask_planFailure_throwsChatbotException() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        when(chatbotPlanService.createPlan(eq("hello"), any()))
                .thenThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "llm error", HttpStatus.INTERNAL_SERVER_ERROR));

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("hello");

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("hello"), any());
        verifyNoInteractions(chatbotQueryService);
    }

    @Test
    void ask_dbPlanWithoutSql_throwsChatbotException() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(true, "USER_BOOKING_LIST", "Fetch booking list", "", "");
        when(chatbotPlanService.createPlan(eq("booking status"), any())).thenReturn(plan);
        when(chatbotQueryService.execute(plan)).thenThrow(
                new ApiException("CHATBOT_INTERNAL_ERROR", "LLM 계획에 DB 조회 SQL이 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        ChatbotRequest.AskDTO request = new ChatbotRequest.AskDTO();
        request.setMessage("booking status");

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("booking status"), any());
    }
}
