package com.example.travel_platform.booking;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 예약 생성 요청 바디를 정의하는 DTO 클래스
public class BookingRequest {

    // 예약 생성 요청 DTO
    @Data
    public static class CreateBookingDTO {
        @NotNull
        private Integer tripPlanId;
        @NotBlank
        private String lodgingName;
        @NotNull
        private LocalDate checkIn;
        @NotNull
        private LocalDate checkOut;
        @NotNull
        @Min(1)
        private Integer guestCount;
        @NotNull
        @Min(0)
        private Integer totalPrice;
    }
}
