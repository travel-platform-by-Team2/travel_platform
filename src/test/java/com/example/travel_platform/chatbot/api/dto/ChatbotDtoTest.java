package com.example.travel_platform.chatbot.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ChatbotDtoTest {

    @Test
    void requestDto_setterGetter_works() {
        ChatbotRequest.ContextDTO contextDTO = new ChatbotRequest.ContextDTO();
        contextDTO.setPage("/bookings/checkout");
        contextDTO.setTripPlanId(3);

        ChatbotRequest.AskDTO askDTO = new ChatbotRequest.AskDTO();
        askDTO.setMessage("booking list");
        askDTO.setContext(contextDTO);

        assertEquals("booking list", askDTO.getMessage());
        assertNotNull(askDTO.getContext());
        assertEquals("/bookings/checkout", askDTO.getContext().getPage());
        assertEquals(3, askDTO.getContext().getTripPlanId());
    }

    @Test
    void responseDto_builder_works() {
        ChatbotResponse.AskDTO response = ChatbotResponse.AskDTO.builder()
                .processingType("DIRECT_LLM")
                .answer("ok")
                .meta(ChatbotResponse.MetaDTO.builder()
                        .needsDb(false)
                        .build())
                .build();

        assertEquals("DIRECT_LLM", response.getProcessingType());
        assertEquals("ok", response.getAnswer());
        assertNotNull(response.getMeta());
        assertEquals(false, response.getMeta().getNeedsDb());
    }
}
