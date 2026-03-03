package com.example.travel_platform.calendar;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 캘린더 일정 생성/수정 요청 바디를 정의하는 DTO 클래스
public class CalendarRequest {

    // 일정 생성 요청 DTO
    @Data
    public static class CreateEventDTO {
        private Integer tripPlanId;
        @NotBlank
        private String title;
        @NotNull
        private LocalDateTime startAt;
        @NotNull
        private LocalDateTime endAt;
        @NotBlank
        private String eventType;
    }

    // 일정 수정 요청 DTO
    @Data
    public static class UpdateEventDTO {
        @NotBlank
        private String title;
        @NotNull
        private LocalDateTime startAt;
        @NotNull
        private LocalDateTime endAt;
        @NotBlank
        private String eventType;
    }
}
