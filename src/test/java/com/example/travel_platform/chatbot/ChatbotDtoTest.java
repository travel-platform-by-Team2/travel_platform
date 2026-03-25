package com.example.travel_platform.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChatbotDtoTest {

    @Test
    void askRequestFactory_createsRequest() {
        ChatbotRequest.ContextDTO context = ChatbotRequest.ContextDTO.createContext("/trip", 10);
        List<ChatbotRequest.HistoryItemDTO> history = List.of(
                ChatbotRequest.HistoryItemDTO.createHistoryItem("user", "이전 질문"),
                ChatbotRequest.HistoryItemDTO.createHistoryItem("assistant", "이전 답변"));

        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("여행 일정 알려줘", context, history);

        assertEquals("여행 일정 알려줘", request.getMessage());
        assertNotNull(request.getContext());
        assertEquals("/trip", request.getContext().getPage());
        assertEquals(10, request.getContext().getTripPlanId());
        assertEquals(2, request.getHistory().size());
        assertEquals("user", request.getHistory().get(0).getRole());
        assertEquals("이전 질문", request.getHistory().get(0).getContent());
    }

    @Test
    void askResponseFactory_createsResponse() {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.createAskResponse(
                "DB_QA",
                "예약이 있어요.",
                List.of("BOOKING", "TRIP"),
                true);

        assertEquals("DB_QA", response.getMode());
        assertEquals("DB_QA", response.getProcessingType());
        assertEquals("예약이 있어요.", response.getAnswer());
        assertEquals(List.of("BOOKING", "TRIP"), response.getUsedTools());
        assertEquals(true, response.getHasSufficientData());
    }
}
