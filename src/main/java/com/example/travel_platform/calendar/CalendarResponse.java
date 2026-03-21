package com.example.travel_platform.calendar;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

public class CalendarResponse {

    @Data
    @Builder
    public static class CalendarPageDTO {
        private String pageTitle;

        public static CalendarPageDTO createCalendarPage() {
            return CalendarPageDTO.builder()
                    .pageTitle("TravelMate | 캘린더")
                    .build();
        }
    }

    @Data
    @Builder
    public static class EventDTO {
        private Integer id;
        private Integer tripPlanId;
        private String title;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String eventType;
        private String memo;

        public static EventDTO fromCalendarEvent(CalendarEvent event) {
            return EventDTO.builder()
                    .id(event.getId())
                    .tripPlanId(event.getTripPlan() == null ? null : event.getTripPlan().getId())
                    .title(event.getTitle())
                    .startAt(event.getStartAt())
                    .endAt(event.getEndAt())
                    .eventType(event.getEventTypeCode())
                    .memo(event.getMemo())
                    .build();
        }
    }

}
