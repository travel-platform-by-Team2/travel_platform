package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

// 예약 조회 응답 포맷을 정의하는 DTO 클래스
public class BookingResponse {

    // 예약 목록 아이템 응답 DTO
    @Data
    @Builder
    public static class BookingSummaryDTO {
        private Integer id;
        private String lodgingName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer totalPrice;
    }

    // 예약 상세 응답 DTO
    @Data
    @Builder
    public static class BookingDetailDTO {
        private Integer id;
        private Integer tripPlanId;
        private String lodgingName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guestCount;
        private Integer totalPrice;
        private LocalDateTime createdAt;
    }
}
