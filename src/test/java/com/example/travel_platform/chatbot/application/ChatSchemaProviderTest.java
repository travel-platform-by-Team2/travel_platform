package com.example.travel_platform.chatbot.application;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ChatSchemaProviderTest {

    @Test
    void schema() {
        ChatSchemaProvider provider = new ChatSchemaProvider();
        String schema = provider.getSchemaContext();

        assertTrue(schema.contains("\"price_per_night\""));
        assertTrue(schema.contains("\"tax_and_service_fee\""));
        assertTrue(schema.contains("\"trip_day\""));
        assertTrue(schema.contains("\"room_name\""));
        assertTrue(schema.contains("\"region_key\""));
        assertFalse(schema.contains("\"like_count\""));
    }
}

