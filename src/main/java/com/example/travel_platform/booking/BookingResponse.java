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

    @Data
    @Builder
    public static class MapDetailPageDTO {
        private String kakaoMapAppKey;

        public static MapDetailPageDTO of(String kakaoMapAppKey) {
            return MapDetailPageDTO.builder()
                    .kakaoMapAppKey(normalize(kakaoMapAppKey))
                    .build();
        }
    }

    @Data
    @Builder
    public static class CheckoutPageDTO {
        private String lodgingName;
        private String roomName;
        private String address;
        private String imageUrl;
        private String checkIn;
        private String checkOut;
        private String nightsLabel;
        private String guests;
        private String roomPriceText;
        private String roomSubtotalText;
        private String feeText;
        private String totalPriceText;
        private Integer roomPrice;
        private Integer totalFee;
        private Long totalPriceRaw;
        private String bookerName;
        private String bookerEmail;
        private String bookerPhone;

        public static CheckoutPageDTO of(
                String lodgingName,
                String roomName,
                String address,
                String imageUrl,
                String checkIn,
                String checkOut,
                String nightsLabel,
                String guests,
                Integer roomPrice,
                long roomSubtotal,
                long feeSubtotal,
                long totalPrice,
                String bookerName,
                String bookerEmail,
                String bookerPhone) {
            return CheckoutPageDTO.builder()
                    .lodgingName(defaultText(lodgingName, "숙소"))
                    .roomName(defaultText(roomName, "기본 객실"))
                    .address(defaultText(address, "주소 정보 없음"))
                    .imageUrl(normalize(imageUrl))
                    .checkIn(normalize(checkIn))
                    .checkOut(normalize(checkOut))
                    .nightsLabel(defaultText(nightsLabel, "1박"))
                    .guests(defaultText(guests, "성인 2명"))
                    .roomPriceText(formatWon(roomPrice))
                    .roomSubtotalText(formatWon(roomSubtotal))
                    .feeText(formatWon(feeSubtotal))
                    .totalPriceText(formatWon(totalPrice))
                    .roomPrice(roomPrice)
                    .totalFee((int) feeSubtotal)
                    .totalPriceRaw(totalPrice)
                    .bookerName(normalize(bookerName))
                    .bookerEmail(normalize(bookerEmail))
                    .bookerPhone(normalize(bookerPhone))
                    .build();
        }
    }

    @Data
    @Builder
    public static class CompletePageDTO {
        private String bookingNumber;
        private String lodgingName;
        private String roomName;
        private String region;
        private String regionKey;
        private String guests;
        private String checkIn;
        private String checkOut;
        private String nightsLabel;
        private String totalPriceText;
        private String completeImageUrl;

        public static CompletePageDTO of(
                String bookingNumber,
                String lodgingName,
                String roomName,
                String region,
                String regionKey,
                String guests,
                String checkIn,
                String checkOut,
                String nightsLabel,
                String totalPriceText,
                String completeImageUrl) {
            return CompletePageDTO.builder()
                    .bookingNumber(normalize(bookingNumber))
                    .lodgingName(defaultText(lodgingName, "숙소"))
                    .roomName(defaultText(roomName, "기본 객실"))
                    .region(defaultText(region, "지역 정보 없음"))
                    .regionKey(defaultText(regionKey, "busan"))
                    .guests(defaultText(guests, "성인 2명"))
                    .checkIn(normalize(checkIn))
                    .checkOut(normalize(checkOut))
                    .nightsLabel(defaultText(nightsLabel, "1박"))
                    .totalPriceText(defaultText(totalPriceText, "0원"))
                    .completeImageUrl(normalize(completeImageUrl))
                    .build();
        }
    }

    // 예약 목록 아이템 응답 DTO
    @Data
    @Builder
    public static class BookingSummaryDTO {
        private Integer id;
        private String lodgingName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer pricePerNight;
        private Integer taxAndServiceFee;
        private String location;
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
        private Integer pricePerNight;
        private Integer taxAndServiceFee;
        private String location;
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

        public static RoomDTO of(
                String name,
                String content,
                String baseCount,
                String maxCount,
                String imageUrl,
                List<String> allImages) {
            return RoomDTO.builder()
                    .name(defaultText(name, "기본 객실"))
                    .content(normalize(content))
                    .baseCount(normalize(baseCount))
                    .maxCount(normalize(maxCount))
                    .imageUrl(normalize(imageUrl))
                    .allImages(allImages == null ? List.of() : allImages)
                    .build();
        }
    }

    @Data
    @Builder
    public static class PlaceImageDTO {
        private String imageUrl;
        private String name;

        public static PlaceImageDTO of(String imageUrl, String name) {
            return PlaceImageDTO.builder()
                    .imageUrl(normalize(imageUrl))
                    .name(normalize(name))
                    .build();
        }
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

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value;
    }

    private static String defaultText(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static String formatWon(long value) {
        return String.format("%,d원", value);
    }
}
