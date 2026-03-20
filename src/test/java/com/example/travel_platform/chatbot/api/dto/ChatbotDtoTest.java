package com.example.travel_platform.chatbot.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ChatbotDtoTest {

    @Test
    void req() {
        ChatbotRequest.ContextDTO contextDTO = ChatbotRequest.ContextDTO.of("/bookings/checkout", 3);
        ChatbotRequest.AskDTO askDTO = ChatbotRequest.AskDTO.of("booking list", contextDTO);

        assertEquals("booking list", askDTO.getMessage());
        assertNotNull(askDTO.getContext());
        assertEquals("/bookings/checkout", askDTO.getContext().getPage());
        assertEquals(3, askDTO.getContext().getTripPlanId());
    }

    @Test
    void resp() {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.of(
                "DIRECT_LLM",
                "ok",
                ChatbotResponse.MetaDTO.direct());

        assertEquals("DIRECT_LLM", response.getProcessingType());
        assertEquals("ok", response.getAnswer());
        assertNotNull(response.getMeta());
        assertEquals(false, response.getMeta().getNeedsDb());
    }

    @Test
    void db() {
        ChatbotResponse.MetaDTO meta = ChatbotResponse.MetaDTO.db("예약 조회", "select * from booking_tb limit 5", 3);

        assertEquals(true, meta.getNeedsDb());
        assertEquals("예약 조회", meta.getQuerySummary());
        assertEquals("select * from booking_tb limit 5", meta.getGeneratedSql());
        assertEquals(3, meta.getRowCount());
    }
}
