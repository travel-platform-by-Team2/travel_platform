package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

// 캘린더 일정 조회 응답 포맷을 정의하는 DTO 클래스
public class CalendarResponse {

    // 일정 단건 응답 DTO
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

        public static EventDTO from(CalendarEvent event) {
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

    // 캘린더 일자 노드 응답 DTO
    @Data
    @Builder
    public static class DayNodeDTO {
        private LocalDate date;
        private Integer eventCount;
        private List<EventDTO> events;
    }
}
