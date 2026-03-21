package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmSearchReview;
import com.example.travel_platform.chatbot.infra.llm.ChatbotSearchAttempt;

class ChatbotOrchestratorTest {

    @Test
    void direct() {
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

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("hello", null);

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
    void dbOnce() {
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
        ChatbotQueryService.QueryResult queryResult = new ChatbotQueryService.QueryResult(
                sql,
                "Fetch booking list",
                rows);
        ChatbotLlmSearchReview review = new ChatbotLlmSearchReview(
                false,
                "USER_BOOKING_LIST",
                "",
                "",
                "현재 결과로 답변 가능하다.");

        when(chatbotPlanService.createPlan(eq("booking list"), any())).thenReturn(plan);
        when(chatbotQueryService.execute(sql, "Fetch booking list")).thenReturn(queryResult);
        when(chatbotPlanService.reviewSearch(eq("booking list"), any(), eq("USER_BOOKING_LIST"), anyList(), eq(5)))
                .thenReturn(review);
        when(chatbotAnswerService.createDbAnswer(
                eq("booking list"),
                eq("USER_BOOKING_LIST"),
                argThat((List<ChatbotSearchAttempt> attempts) -> attempts.size() == 1
                        && attempts.get(0).rows().size() == 1
                        && attempts.get(0).evaluationReason().contains("답변 가능")),
                eq(false)))
                .thenReturn("db answer");

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("booking list", null);

        ChatbotResponse.AskDTO response = chatbotOrchestrator.ask(request);

        assertEquals("DB_QUERY", response.getProcessingType());
        assertNotNull(response.getMeta());
        assertEquals(true, response.getMeta().getNeedsDb());
        assertEquals(1, response.getMeta().getRowCount());
        assertEquals("Fetch booking list", response.getMeta().getQuerySummary());
        assertEquals(sql, response.getMeta().getGeneratedSql());
        assertEquals("db answer", response.getAnswer());
        verify(chatbotQueryService).execute(sql, "Fetch booking list");
        verify(chatbotAnswerService).createDbAnswer(eq("booking list"), eq("USER_BOOKING_LIST"), anyList(), eq(false));
    }

    @Test
    void dbRetry() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);

        String question = "제주도 관련 게시글이 있어?";
        String firstSql = "select id, title from board_tb where title like '%제주도%' limit 5";
        String secondSql = "select id, title, start_at from calendar_event_tb where title like '%제주%' limit 5";
        String thirdSql = "select id, title from board_tb where title like '%제주%' limit 5";

        ChatbotLlmPlan initialPlan = new ChatbotLlmPlan(
                true,
                "BOARD_SEARCH",
                "제주도 게시글 검색",
                firstSql,
                "");

        ChatbotQueryService.QueryResult firstResult = new ChatbotQueryService.QueryResult(
                firstSql,
                "제주도 게시글 검색",
                List.of());
        ChatbotQueryService.QueryResult secondResult = new ChatbotQueryService.QueryResult(
                secondSql,
                "제주 일정 검색",
                List.of(Map.of("title", "제주 출발", "start_at", "2026-03-01")));
        ChatbotQueryService.QueryResult thirdResult = new ChatbotQueryService.QueryResult(
                thirdSql,
                "제주 관련 게시글 검색",
                List.of(Map.of("title", "제주 여행 코스 추천")));

        ChatbotLlmSearchReview firstReview = new ChatbotLlmSearchReview(
                true,
                "BOARD_SEARCH",
                "제주 일정 검색",
                secondSql,
                "게시글 결과가 비어 있어 제주 키워드로 관련 데이터를 더 탐색한다.");
        ChatbotLlmSearchReview secondReview = new ChatbotLlmSearchReview(
                true,
                "BOARD_SEARCH",
                "제주 관련 게시글 검색",
                thirdSql,
                "일정 데이터만 확인되어 게시글 테이블을 다시 조회한다.");
        ChatbotLlmSearchReview thirdReview = new ChatbotLlmSearchReview(
                false,
                "BOARD_SEARCH",
                "",
                "",
                "제주 관련 게시글을 찾아 답변할 수 있다.");

        when(chatbotPlanService.createPlan(eq(question), any())).thenReturn(initialPlan);
        when(chatbotQueryService.execute(firstSql, "제주도 게시글 검색")).thenReturn(firstResult);
        when(chatbotQueryService.execute(secondSql, "제주 일정 검색")).thenReturn(secondResult);
        when(chatbotQueryService.execute(thirdSql, "제주 관련 게시글 검색")).thenReturn(thirdResult);
        when(chatbotPlanService.reviewSearch(eq(question), any(), eq("BOARD_SEARCH"), anyList(), eq(5)))
                .thenReturn(firstReview, secondReview, thirdReview);
        when(chatbotAnswerService.createDbAnswer(
                eq(question),
                eq("BOARD_SEARCH"),
                argThat((List<ChatbotSearchAttempt> attempts) -> attempts.size() == 3
                        && attempts.get(0).evaluationReason().contains("관련 데이터를 더 탐색")
                        && attempts.get(1).evaluationReason().contains("게시글 테이블을 다시 조회")
                        && attempts.get(2).evaluationReason().contains("답변할 수 있다.")),
                eq(false)))
                .thenReturn("있어요. '제주 여행 코스 추천' 게시글이 있습니다.");

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest(question, null);

        ChatbotResponse.AskDTO response = chatbotOrchestrator.ask(request);

        assertEquals("DB_QUERY", response.getProcessingType());
        assertEquals("있어요. '제주 여행 코스 추천' 게시글이 있습니다.", response.getAnswer());
        assertEquals("제주 관련 게시글 검색", response.getMeta().getQuerySummary());
        assertEquals(thirdSql, response.getMeta().getGeneratedSql());
        assertEquals(1, response.getMeta().getRowCount());

        verify(chatbotQueryService).execute(firstSql, "제주도 게시글 검색");
        verify(chatbotQueryService).execute(secondSql, "제주 일정 검색");
        verify(chatbotQueryService).execute(thirdSql, "제주 관련 게시글 검색");
        verify(chatbotPlanService, times(3)).reviewSearch(eq(question), any(), eq("BOARD_SEARCH"), anyList(), eq(5));
        verify(chatbotAnswerService).createDbAnswer(eq(question), eq("BOARD_SEARCH"), anyList(), eq(false));
    }

    @Test
    void dbCap() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);

        ChatbotLlmPlan initialPlan = new ChatbotLlmPlan(
                true,
                "CALENDAR_SEARCH",
                "제주도 일정 검색",
                "select id, title from calendar_event_tb where title like '%제주도%' limit 5",
                "");

        when(chatbotPlanService.createPlan(eq("제주도 일정의 시작일이 언제였지?"), any())).thenReturn(initialPlan);
        when(chatbotQueryService.execute(anyString(), anyString())).thenReturn(
                new ChatbotQueryService.QueryResult(
                        "select id, title from calendar_event_tb where title like '%제주도%' limit 5",
                        "1차 검색",
                        List.of()),
                new ChatbotQueryService.QueryResult(
                        "select id, title, start_at from calendar_event_tb where title like '%제주%' limit 5",
                        "2차 검색",
                        List.of()),
                new ChatbotQueryService.QueryResult(
                        "select id, title from trip_plan_tb where title like '%제주%' limit 5",
                        "3차 검색",
                        List.of()),
                new ChatbotQueryService.QueryResult(
                        "select id, title from board_tb where title like '%제주%' limit 5",
                        "4차 검색",
                        List.of()),
                new ChatbotQueryService.QueryResult(
                        "select id, title, start_at from calendar_event_tb where title like '%출발%' limit 5",
                        "5차 검색",
                        List.of()));
        when(chatbotPlanService.reviewSearch(eq("제주도 일정의 시작일이 언제였지?"), any(), eq("CALENDAR_SEARCH"), anyList(), eq(5)))
                .thenReturn(
                        new ChatbotLlmSearchReview(true, "CALENDAR_SEARCH", "2차 검색",
                                "select id, title, start_at from calendar_event_tb where title like '%제주%' limit 5",
                                "정확히 일치하는 일정이 없어 키워드를 넓힌다."),
                        new ChatbotLlmSearchReview(true, "CALENDAR_SEARCH", "3차 검색",
                                "select id, title from trip_plan_tb where title like '%제주%' limit 5",
                                "캘린더 결과가 비어 있어 여행 계획 데이터도 확인한다."),
                        new ChatbotLlmSearchReview(true, "CALENDAR_SEARCH", "4차 검색",
                                "select id, title from board_tb where title like '%제주%' limit 5",
                                "여행 계획에서도 단서를 못 찾아 커뮤니티 데이터까지 확인한다."),
                        new ChatbotLlmSearchReview(true, "CALENDAR_SEARCH", "5차 검색",
                                "select id, title, start_at from calendar_event_tb where title like '%출발%' limit 5",
                                "출발 키워드로 일정을 다시 확인한다."),
                        new ChatbotLlmSearchReview(true, "CALENDAR_SEARCH", "6차 검색",
                                "select id, title, start_at from calendar_event_tb where title like '%여행%' limit 5",
                                "아직 단서를 못 찾았지만 더 탐색이 필요하다."));
        when(chatbotAnswerService.createDbAnswer(
                eq("제주도 일정의 시작일이 언제였지?"),
                eq("CALENDAR_SEARCH"),
                argThat((List<ChatbotSearchAttempt> attempts) -> attempts.size() == 5
                        && attempts.get(4).evaluationReason().contains("더 탐색이 필요하다.")),
                eq(true)))
                .thenReturn("현재 확인한 일정 데이터만으로는 제주도 일정의 시작일을 확정하기 어렵습니다.");

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("제주도 일정의 시작일이 언제였지?", null);

        ChatbotResponse.AskDTO response = chatbotOrchestrator.ask(request);

        assertEquals("DB_QUERY", response.getProcessingType());
        assertEquals("현재 확인한 일정 데이터만으로는 제주도 일정의 시작일을 확정하기 어렵습니다.", response.getAnswer());
        assertEquals("5차 검색", response.getMeta().getQuerySummary());
        assertEquals("select id, title, start_at from calendar_event_tb where title like '%출발%' limit 5",
                response.getMeta().getGeneratedSql());
        assertEquals(0, response.getMeta().getRowCount());

        verify(chatbotQueryService, times(5)).execute(anyString(), anyString());
        verify(chatbotPlanService, times(5)).reviewSearch(
                eq("제주도 일정의 시작일이 언제였지?"),
                any(),
                eq("CALENDAR_SEARCH"),
                anyList(),
                eq(5));
        verify(chatbotAnswerService).createDbAnswer(
                eq("제주도 일정의 시작일이 언제였지?"),
                eq("CALENDAR_SEARCH"),
                anyList(),
                eq(true));
    }

    @Test
    void qErr() {
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
        when(chatbotQueryService.execute(sql, "Fetch booking list"))
                .thenThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "db error", HttpStatus.INTERNAL_SERVER_ERROR));

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("booking status", null);

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("booking status"), any());
    }

    @Test
    void planErr() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        when(chatbotPlanService.createPlan(eq("hello"), any()))
                .thenThrow(new ApiException("CHATBOT_INTERNAL_ERROR", "llm error", HttpStatus.INTERNAL_SERVER_ERROR));

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("hello", null);

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("hello"), any());
        verifyNoInteractions(chatbotQueryService);
    }

    @Test
    void noSql() {
        ChatbotPlanService chatbotPlanService = mock(ChatbotPlanService.class);
        ChatbotQueryService chatbotQueryService = mock(ChatbotQueryService.class);
        ChatbotAnswerService chatbotAnswerService = mock(ChatbotAnswerService.class);
        ChatbotOrchestrator chatbotOrchestrator = new ChatbotOrchestrator(
                chatbotPlanService,
                chatbotQueryService,
                chatbotAnswerService);
        ChatbotLlmPlan plan = new ChatbotLlmPlan(true, "USER_BOOKING_LIST", "Fetch booking list", "", "");
        when(chatbotPlanService.createPlan(eq("booking status"), any())).thenReturn(plan);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("booking status", null);

        ApiException exception = assertThrows(ApiException.class, () -> chatbotOrchestrator.ask(request));
        assertEquals("CHATBOT_INTERNAL_ERROR", exception.getCode());
        verify(chatbotPlanService).createPlan(eq("booking status"), any());
        verifyNoInteractions(chatbotQueryService);
    }
}

