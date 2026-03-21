package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;

class BookingResponseTest {

    @Test
    void map() {
        BookingResponse.MapDetailPageDTO page = BookingResponse.MapDetailPageDTO.createMapDetailPage(null);

        assertEquals("", page.getKakaoMapAppKey());
    }

    @Test
    void co() {
        BookingResponse.CheckoutPageDTO page = BookingResponse.CheckoutPageDTO.createCheckoutPage(
                "",
                "",
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
        assertEquals("busan", page.getRegionKey());
        assertEquals("부산", page.getRegionLabel());
        assertEquals("", page.getImageUrl());
        assertEquals("1박", page.getNightsLabel());
        assertEquals("성인 2명", page.getGuests());
        assertEquals("350,000원", page.getRoomPriceText());
        assertEquals("910,000원", page.getTotalPriceText());
    }

    @Test
    void done() {
        BookingResponse.CompletePageDTO page = BookingResponse.CompletePageDTO.createCompletePage(
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

    @Test
    void summary() {
        Booking booking = booking();

        BookingResponse.BookingSummaryDTO dto = BookingResponse.BookingSummaryDTO.fromBooking(booking);

        assertEquals("시그니엘 부산", dto.getLodgingName());
        assertEquals("디럭스 룸", dto.getRoomName());
        assertEquals("busan", dto.getRegionKey());
        assertEquals("부산", dto.getLocation());
    }

    @Test
    void detail() {
        Booking booking = booking();

        BookingResponse.BookingDetailDTO dto = BookingResponse.BookingDetailDTO.fromBooking(booking);

        assertEquals(1, dto.getTripPlanId());
        assertEquals("시그니엘 부산", dto.getLodgingName());
        assertEquals("디럭스 룸", dto.getRoomName());
        assertEquals("busan", dto.getRegionKey());
        assertEquals("부산", dto.getLocation());
    }

    private Booking booking() {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(1);
        TripPlan tripPlan = TripPlan.create(
                user,
                "부산 여행",
                "busan",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                null);
        tripPlan.setId(1);

        Booking booking = Booking.create(
                user,
                tripPlan,
                "시그니엘 부산",
                "디럭스 룸",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                2,
                450000,
                200000,
                "busan",
                "https://image.test/hotel.jpg");
        booking.setId(9);
        booking.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));
        return booking;
    }
}
