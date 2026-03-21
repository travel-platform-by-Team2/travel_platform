package com.example.travel_platform.booking.mapPlaceImage;

public interface MapPlaceImageRepositoryCustom {

    void upsertMapPlaceImage(String normalizedName, String placeName, String imageUrl, String source);
}
