package com.example.travel_platform.booking.lodging;

public record LodgingPoiRow(
        String externalPlaceId,
        String name,
        String phone,
        String address,
        String roadAddress,
        String placeUrl,
        String categoryName,
        String categoryGroupCode,
        Double lat,
        Double lng) {
}
