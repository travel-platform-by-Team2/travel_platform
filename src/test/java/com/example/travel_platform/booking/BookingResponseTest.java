package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookingResponseTest {

    @Test
    void map() {
        BookingResponse.MapDetailPageDTO page = BookingResponse.MapDetailPageDTO.of(null);

        assertEquals("", page.getKakaoMapAppKey());
    }

    @Test
    void co() {
        BookingResponse.CheckoutPageDTO page = BookingResponse.CheckoutPageDTO.of(
                "",
                "",
                "",
                null,
                "2026-04-10",
                "2026-04-12",
                "",
                "",
                350000,
                700000,
                210000,
                910000,
                null,
                null,
                null);

        assertEquals("숙소", page.getLodgingName());
        assertEquals("기본 객실", page.getRoomName());
        assertEquals("주소 정보 없음", page.getAddress());
        assertEquals("", page.getImageUrl());
        assertEquals("1박", page.getNightsLabel());
        assertEquals("성인 2명", page.getGuests());
        assertEquals("350,000원", page.getRoomPriceText());
        assertEquals("910,000원", page.getTotalPriceText());
    }

    @Test
    void done() {
        BookingResponse.CompletePageDTO page = BookingResponse.CompletePageDTO.of(
                "KR-20260320-123",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "https://image.test/complete.jpg");

        assertEquals("숙소", page.getLodgingName());
        assertEquals("기본 객실", page.getRoomName());
        assertEquals("지역 정보 없음", page.getRegion());
        assertEquals("busan", page.getRegionKey());
        assertEquals("성인 2명", page.getGuests());
        assertEquals("1박", page.getNightsLabel());
        assertEquals("0원", page.getTotalPriceText());
        assertEquals("https://image.test/complete.jpg", page.getCompleteImageUrl());
    }
}
