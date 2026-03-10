package com.example.travel_platform.calendar;

import java.time.LocalDateTime;

import lombok.Data;

public class CalendarRequest {

    @Data
    public static class CreateEventDTO {
        private Integer tripPlanId;
        private String title;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String eventType;
        private String memo;
    }

    @Data
    public static class UpdateEventDTO {
        private String title;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String eventType;
        private String memo;
    }
}
