package com.example.travel_platform.chatbot.infra.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatbotLlmPlanTest {

    @Test
    void plan() {
        ChatbotLlmPlan plan = new ChatbotLlmPlan(
                true,
                "USER_BOOKING_LIST",
                "Fetch booking list",
                "select id from booking_tb limit 5",
                "");

        assertEquals(true, plan.needsDb());
        assertEquals("USER_BOOKING_LIST", plan.queryIntent());
        assertEquals("Fetch booking list", plan.querySummary());
        assertEquals("select id from booking_tb limit 5", plan.sql());
        assertEquals("", plan.answer());
    }
}

