package com.example.travel_platform.chatbot.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChatbotDtoTest {

    @Test
    void askRequestFactory_createsRequest() {
        ChatbotRequest.ContextDTO context = ChatbotRequest.ContextDTO.createContext("/trip", 10);
        ChatbotRequest.AskDTO request = ChatbotRequest.AskDTO.createAskRequest("여행 일정 알려줘", context);

        assertEquals("여행 일정 알려줘", request.getMessage());
        assertNotNull(request.getContext());
        assertEquals("/trip", request.getContext().getPage());
        assertEquals(10, request.getContext().getTripPlanId());
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
