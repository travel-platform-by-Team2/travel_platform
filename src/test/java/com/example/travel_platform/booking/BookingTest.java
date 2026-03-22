package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;

class BookingTest {

    @Test
    void booked() {
        Booking booking = booking();

        assertEquals(BookingStatus.BOOKED, booking.getStatus());
        assertEquals("booked", booking.getStatusCode());
        assertEquals("예약 확정", booking.getStatusLabel());
        assertNull(booking.getCancelledAt());
    }

    @Test
    void cancel() {
        Booking booking = booking();
        LocalDateTime cancelledAt = LocalDateTime.of(2026, 3, 21, 18, 0);

        booking.cancel(cancelledAt);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("cancelled", booking.getStatusCode());
        assertEquals("예약 취소", booking.getStatusLabel());
        assertEquals(cancelledAt, booking.getCancelledAt());
    }

    private Booking booking() {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(1);

        TripPlan tripPlan = TripPlan.create(
                user,
                "제주 여행",
                "jeju",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                null);
        tripPlan.setId(1);

        return Booking.create(
                user,
                tripPlan,
                "제주 오션뷰 호텔",
                "오션뷰 스탠다드",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                2,
                280000,
                50400,
                "jeju",
                null);
    }
}
