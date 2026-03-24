package com.example.travel_platform.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.weather.WeatherRegion;
import com.example.travel_platform.weather.WeatherResponse;
import com.example.travel_platform.weather.WeatherService;

class ChatbotServiceTest {

    private static final List<ChatbotRequest.HistoryItemDTO> HISTORY = List.of(
            ChatbotRequest.HistoryItemDTO.createHistoryItem("user", "이전 질문"),
            ChatbotRequest.HistoryItemDTO.createHistoryItem("assistant", "이전 답변"));

    @Test
    void ask_returnsWeatherAnswer_whenInterpretationUsesWeatherTool() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        LocalDate targetDate = LocalDate.of(2026, 3, 28);
        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("3월 28일 제주 날씨 알려줘", null, HISTORY);
        ChatbotService.QueryPlan weatherPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                "jeju",
                "2026-03-28",
                null,
                null,
                4);
        ChatbotService.ResolvedContext resolvedContext = ChatbotService.ResolvedContext.createResolvedContext(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                "jeju",
                "2026-03-28",
                null,
                "jeju",
                null,
                null,
                4,
                false,
                List.of());
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(weatherPlan),
                resolvedContext);
        WeatherResponse.WeatherDTO weatherResponse = WeatherResponse.WeatherDTO.createWeather(
                WeatherRegion.JEJU,
                targetDate,
                List.of(
                        WeatherResponse.DailyForecastDTO.createDailyForecast(
                                targetDate,
                                "토",
                                11,
                                18,
                                "맑음",
                                "구름많음",
                                "맑음 / 구름많음",
                                10,
                                20,
                                20)));

        given(chatbotLlmClient.interpret(eq("3월 28일 제주 날씨 알려줘"), any(), eq(HISTORY), any())).willReturn(interpretation);
        given(weatherService.getWeather("jeju", targetDate)).willReturn(weatherResponse);
        given(chatbotLlmClient.answerDbQa(eq("3월 28일 제주 날씨 알려줘"), eq(null), eq(HISTORY), eq(interpretation), any()))
                .willReturn("제주 3월 28일 날씨는 맑음, 11~18도예요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals("제주 3월 28일 날씨는 맑음, 11~18도예요.", response.getAnswer());
        assertEquals(List.of("WEATHER"), response.getUsedTools());
        assertEquals(true, response.getHasSufficientData());
        verifyNoInteractions(chatQueryRepository);
    }

    @Test
    void ask_usesResolvedContext_whenWeatherPlanIsMissingSlots() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        LocalDate targetDate = LocalDate.of(2026, 3, 28);
        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("3월 28일 말야", null, HISTORY);
        ChatbotService.QueryPlan weatherPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                null,
                null,
                null,
                null,
                4);
        ChatbotService.ResolvedContext resolvedContext = ChatbotService.ResolvedContext.createResolvedContext(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                "jeju",
                "2026-03-28",
                null,
                null,
                null,
                null,
                4,
                true,
                List.of());
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(weatherPlan),
                resolvedContext);
        WeatherResponse.WeatherDTO weatherResponse = WeatherResponse.WeatherDTO.createWeather(
                WeatherRegion.JEJU,
                targetDate,
                List.of(
                        WeatherResponse.DailyForecastDTO.createDailyForecast(
                                targetDate,
                                "토",
                                11,
                                18,
                                "맑음",
                                "구름많음",
                                "맑음 / 구름많음",
                                10,
                                20,
                                20)));

        given(chatbotLlmClient.interpret(eq("3월 28일 말야"), any(), eq(HISTORY), any())).willReturn(interpretation);
        given(weatherService.getWeather("jeju", targetDate)).willReturn(weatherResponse);
        given(chatbotLlmClient.answerDbQa(eq("3월 28일 말야"), eq(null), eq(HISTORY), eq(interpretation), any()))
                .willReturn("제주 3월 28일 날씨는 맑음, 11~18도예요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals(true, response.getHasSufficientData());
        assertEquals(List.of("WEATHER"), response.getUsedTools());
        verify(weatherService).getWeather("jeju", targetDate);
    }

    @Test
    void ask_returnsWeatherClarification_whenWeatherPlanIsMissingRegionOrDate() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("제주 날씨 알려줘", null, HISTORY);
        ChatbotService.QueryPlan weatherPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                "jeju",
                null,
                null,
                null,
                4);
        ChatbotService.ResolvedContext resolvedContext = ChatbotService.ResolvedContext.createResolvedContext(
                ChatbotService.Domain.WEATHER,
                "DATE_FORECAST",
                "jeju",
                null,
                null,
                "jeju",
                null,
                null,
                4,
                true,
                List.of("targetDate"));
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(weatherPlan),
                resolvedContext);

        given(chatbotLlmClient.interpret(eq("제주 날씨 알려줘"), any(), eq(HISTORY), any())).willReturn(interpretation);

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals(false, response.getHasSufficientData());
        assertEquals(List.of("WEATHER"), response.getUsedTools());
        verifyNoInteractions(weatherService);
    }

    @Test
    void ask_returnsGeneralChatResponse_whenInterpretationIsGeneralChat() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("안녕", null, HISTORY);
        given(chatbotLlmClient.interpret(eq("안녕"), any(), eq(HISTORY), any()))
                .willReturn(ChatbotService.Interpretation.createGeneralChatInterpretation());
        given(chatbotLlmClient.answerGeneralChat("안녕", null, HISTORY)).willReturn("안녕하세요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("GENERAL_CHAT", response.getMode());
        assertEquals("안녕하세요.", response.getAnswer());
        assertEquals(true, response.getHasSufficientData());
    }

    @Test
    void ask_returnsClarification_whenQueryResultsAreEmpty() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("내 예약 알려줘", null, HISTORY);
        ChatbotService.QueryPlan queryPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.BOOKING,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                5);
        ChatbotService.ResolvedContext resolvedContext = ChatbotService.ResolvedContext.createResolvedContext(
                ChatbotService.Domain.BOOKING,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                null,
                null,
                5,
                false,
                List.of());
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(queryPlan),
                resolvedContext);

        given(chatbotLlmClient.interpret(eq("내 예약 알려줘"), any(), eq(HISTORY), any())).willReturn(interpretation);
        given(chatQueryRepository.execute(eq(1), eq(null), any()))
                .willReturn(List.of(ChatbotService.QueryBlock.createQueryBlock(
                        ChatbotService.Domain.BOOKING,
                        "UPCOMING_LIST",
                        "예약 조회 결과 0건",
                        List.of())));

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals(false, response.getHasSufficientData());
        assertEquals(List.of("BOOKING"), response.getUsedTools());
    }

    @Test
    void ask_returnsDbAnswer_whenQueryResultsExist() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        WeatherService weatherService = mock(WeatherService.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository, weatherService);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("내 여행 알려줘", null, HISTORY);
        ChatbotService.QueryPlan queryPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.TRIP,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                5);
        ChatbotService.ResolvedContext resolvedContext = ChatbotService.ResolvedContext.createResolvedContext(
                ChatbotService.Domain.TRIP,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                null,
                null,
                5,
                false,
                List.of());
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(queryPlan),
                resolvedContext);
        List<ChatbotService.QueryBlock> queryBlocks = List.of(ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.TRIP,
                "UPCOMING_LIST",
                "여행 계획 조회 결과 1건",
                List.of(Map.of("title", "제주 여행"))));

        given(chatbotLlmClient.interpret(eq("내 여행 알려줘"), any(), eq(HISTORY), any())).willReturn(interpretation);
        given(chatQueryRepository.execute(eq(1), eq(null), any())).willReturn(queryBlocks);
        given(chatbotLlmClient.answerDbQa("내 여행 알려줘", null, HISTORY, interpretation, queryBlocks))
                .willReturn("다가오는 여행이 1건 있어요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals("다가오는 여행이 1건 있어요.", response.getAnswer());
        assertEquals(true, response.getHasSufficientData());
        verify(chatbotLlmClient).answerDbQa("내 여행 알려줘", null, HISTORY, interpretation, queryBlocks);
    }
}
