package com.example.travel_platform.chatbot.application;

import org.springframework.stereotype.Component;

@Component
public class ChatSchemaProvider {

    private static final String SCHEMA_CONTEXT_JSON = """
            {
              "tables": [
                {
                  "name": "booking_tb",
                  "columns": [
                    "id",
                    "user_id",
                    "trip_plan_id",
                    "lodging_name",
                    "check_in",
                    "check_out",
                    "guest_count",
                    "price_per_night",
                    "tax_and_service_fee",
                    "location",
                    "image_url",
                    "created_at"
                  ]
                },
                {
                  "name": "calendar_event_tb",
                  "columns": ["id", "user_id", "trip_plan_id", "title", "start_at", "end_at", "event_type", "memo"]
                },
                {
                  "name": "board_tb",
                  "columns": ["id", "user_id", "title", "category", "content", "view_count", "like_count", "created_at"]
                },
                {
                  "name": "trip_plan_tb",
                  "columns": ["id", "user_id", "title", "region", "who_with", "start_date", "end_date", "img_url", "created_at"]
                },
                {
                  "name": "trip_place_tb",
                  "columns": ["id", "trip_plan_id", "place_name", "address", "latitude", "longitude", "day_order"]
                }
              ]
            }
            """;

    public String getSchemaContext() {
        return SCHEMA_CONTEXT_JSON;
    }
}

