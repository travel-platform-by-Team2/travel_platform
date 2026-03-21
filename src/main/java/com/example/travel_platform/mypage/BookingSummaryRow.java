package com.example.travel_platform.mypage;

import java.time.LocalDate;

public record BookingSummaryRow(
        Integer bookingId,
        String lodgingName,
        LocalDate checkIn,
        LocalDate checkOut) {
}
