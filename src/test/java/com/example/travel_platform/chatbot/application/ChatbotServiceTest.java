package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmClient;

class ChatbotServiceTest {

    @Test
    void ask_returnsGeneralChatResponse_whenInterpretationIsGeneralChat() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("안녕", null);
        given(chatbotLlmClient.interpret(eq("안녕"), any(), any()))
                .willReturn(ChatbotService.Interpretation.createGeneralChatInterpretation());
        given(chatbotLlmClient.answerGeneralChat("안녕", null)).willReturn("안녕하세요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("GENERAL_CHAT", response.getMode());
        assertEquals("안녕하세요.", response.getAnswer());
        assertEquals(true, response.getHasSufficientData());
    }

    @Test
    void ask_returnsClarification_whenQueryResultsAreEmpty() {
        ChatbotLlmClient chatbotLlmClient = mock(ChatbotLlmClient.class);
        ChatQueryRepository chatQueryRepository = mock(ChatQueryRepository.class);
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("내 예약 알려줘", null);
        ChatbotService.QueryPlan queryPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.BOOKING,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                5);
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(queryPlan));

        given(chatbotLlmClient.interpret(eq("내 예약 알려줘"), any(), any())).willReturn(interpretation);
        given(chatQueryRepository.execute(1, null, List.of(queryPlan)))
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
        ChatbotService chatbotService = new ChatbotService(chatbotLlmClient, chatQueryRepository);

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("내 여행 알려줘", null);
        ChatbotService.QueryPlan queryPlan = ChatbotService.QueryPlan.createQueryPlan(
                ChatbotService.Domain.TRIP,
                "UPCOMING_LIST",
                null,
                null,
                null,
                null,
                5);
        ChatbotService.Interpretation interpretation = ChatbotService.Interpretation.createInterpretation(
                ChatbotService.Mode.DB_QA,
                List.of(queryPlan));
        List<ChatbotService.QueryBlock> queryBlocks = List.of(ChatbotService.QueryBlock.createQueryBlock(
                ChatbotService.Domain.TRIP,
                "UPCOMING_LIST",
                "여행 계획 조회 결과 1건",
                List.of(Map.of("title", "제주 여행"))));

        given(chatbotLlmClient.interpret(eq("내 여행 알려줘"), any(), any())).willReturn(interpretation);
        given(chatQueryRepository.execute(1, null, List.of(queryPlan))).willReturn(queryBlocks);
        given(chatbotLlmClient.answerDbQa("내 여행 알려줘", null, interpretation, queryBlocks))
                .willReturn("다가오는 여행이 1건 있어요.");

        ChatbotResponse.AskDTO response = chatbotService.ask(1, request);

        assertEquals("DB_QA", response.getMode());
        assertEquals("다가오는 여행이 1건 있어요.", response.getAnswer());
        assertEquals(true, response.getHasSufficientData());
        verify(chatbotLlmClient).answerDbQa("내 여행 알려줘", null, interpretation, queryBlocks);
    }
}
