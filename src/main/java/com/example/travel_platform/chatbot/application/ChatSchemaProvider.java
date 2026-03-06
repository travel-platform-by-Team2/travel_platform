package com.example.travel_platform.chatbot.application;

import org.springframework.stereotype.Component;

@Component
public class ChatSchemaProvider {

    private static final String SCHEMA_CONTEXT_JSON = """
            {
              "tables": [
                {
                  "name": "booking_tb",
                  "columns": ["id", "lodging_name", "check_in", "check_out", "guest_count", "total_price"]
                },
                {
                  "name": "calendar_event_tb",
                  "columns": ["id", "title", "start_at", "end_at", "event_type"]
                },
                {
                  "name": "board_tb",
                  "columns": ["id", "title", "view_count", "created_at"]
                },
                {
                  "name": "trip_plan_tb",
                  "columns": ["id", "title", "start_date", "end_date"]
                },
                {
                  "name": "trip_place_tb",
                  "columns": ["id", "trip_plan_id"]
                }
              ]
            }
            """;

    public String getSchemaContext() {
        return SCHEMA_CONTEXT_JSON;
    }
}
