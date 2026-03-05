package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

public class BookingRequest {

    @Data
    public static class CreateBookingDTO {
        private Integer tripPlanId;
        private String lodgingName;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private Integer guestCount;
        private Integer totalPrice;
    }

    @Data
    public static class MergeMapPoisDTO {
        private String regionKey;
        private MapBoundsDTO bounds;
        private List<MapPoiDTO> kakaoPois;
    }

    @Data
    public static class MapBoundsDTO {
        private Double swLat;
        private Double swLng;
        private Double neLat;
        private Double neLng;
    }

    @Data
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
