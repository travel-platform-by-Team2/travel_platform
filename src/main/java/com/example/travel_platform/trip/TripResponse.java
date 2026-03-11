package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

// 여행 계획/장소 조회 응답 포맷을 정의하는 DTO 클래스
public class TripResponse {

    // 여행 계획 목록 아이템 응답 DTO
    @Data
    @Builder
    public static class PlanSummaryDTO {
        private Integer id;
        private String title;
        private String imgUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private String placeName;
        private String dDay;
        private boolean disabled;
    }

    // 여행 장소 단건 응답 DTO
    @Data
    @Builder
    public static class PlaceDTO {
        private Integer id;
        private String placeName;
        private String address;
        private Integer dayOrder;
    }

    // 여행 계획 상세 응답 DTO
    @Data
    @Builder
    public static class PlanDetailDTO {
        private Integer id;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<PlaceDTO> places;
    }
}
