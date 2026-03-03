package com.example.travel_platform.trip;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 여행 계획/장소 관련 요청 바디를 정의하는 DTO 클래스
public class TripRequest {

    // 여행 계획 생성 요청 데이터를 담는 DTO
    @Data
    public static class CreatePlanDTO {
        @NotBlank
        private String title;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
    }

    // 여행 장소 추가 요청 데이터를 담는 DTO
    @Data
    public static class AddPlaceDTO {
        @NotBlank
        private String placeName;
        private String address;
        private BigDecimal latitude;
        private BigDecimal longitude;
        @NotNull
        private Integer dayOrder;
    }
}
