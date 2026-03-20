package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BookingRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingDTO {
        private Integer tripPlanId;
        private String lodgingName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guestCount;
        private Integer pricePerNight;
        private Integer taxAndServiceFee;
        private String location;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteBookingDTO {
        private String lodgingName;
        private String roomName;
        private String regionKey;
        private String location;
        private String checkIn;
        private String checkOut;
        private String guests;
        private Integer pricePerNight;
        private Integer taxAndServiceFee;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomQueryDTO {
        private String lodgingName;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceImageQueryDTO {
        private String placeUrl;
        private String name;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeMapPoisDTO {
        private String regionKey;
        private MapBoundsDTO bounds;
        private List<MapPoiDTO> kakaoPois;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapBoundsDTO {
        private Double swLat;
        private Double swLng;
        private Double neLat;
        private Double neLng;
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
