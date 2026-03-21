package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        public static EventDTO fromEvent(CalendarEvent event) {
            return EventDTO.builder()
                    .id(event.getId())
                    .tripPlanId(event.getTripPlan() == null ? null : event.getTripPlan().getId())
                    .title(event.getTitle())
                    .startAt(event.getStartAt())
                    .endAt(event.getEndAt())
                    .eventType(event.getEventType())
                    .memo(event.getMemo())
                    .build();
        }
    }

    @Data
    @Builder
    public static class DayNodeDTO {
        private LocalDate date;
        private Integer eventCount;
        private List<EventDTO> events;

        public static DayNodeDTO createDayNode(LocalDate date, Integer eventCount, List<EventDTO> events) {
            return DayNodeDTO.builder()
                    .date(date)
                    .eventCount(eventCount)
                    .events(events == null ? List.of() : events)
                    .build();
        }
    }
}
