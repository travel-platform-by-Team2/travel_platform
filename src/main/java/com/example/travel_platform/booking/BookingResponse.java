package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Data
    @Builder
    public static class RoomDTO {
        private String name;
        private String content;
        private String baseCount;
        private String maxCount;
        private String imageUrl;
        private List<String> allImages; // 추가 이미지 리스트 필드
    }

    @Data
    @Builder
    public static class PlaceImageDTO {
        private String imageUrl;
        private String name;
    }

    @Data
    @Builder
    public static class MergeMapPoisResponseDTO {
        private List<MapPoiDTO> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapPoiDTO {
        private String externalPlaceId;
        private String name;
        private String phone;
        private String address;
        private String roadAddress;
        private String placeUrl;
        private String categoryName;
        private String categoryGroupCode;
        private Double lat;
        private Double lng;
        private String type;
        private String source;
    }
}
