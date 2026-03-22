package com.example.travel_platform.mypage;

import java.time.LocalDate;

import com.example.travel_platform.booking.BookingResponse;
import com.example.travel_platform.booking.BookingStatus;

public enum BookingCategory {
    ALL("all"),
    UPCOMING("upcoming"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String code;

    BookingCategory(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(BookingResponse.BookingSummaryDTO booking, LocalDate today) {
        if (this == ALL) {
            return true;
        }
        return this == fromBooking(booking, today);
    }

    public static BookingCategory fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ALL;
        }

        for (BookingCategory value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return ALL;
    }

    public static BookingCategory fromBooking(BookingResponse.BookingSummaryDTO booking, LocalDate today) {
        if (BookingStatus.CANCELLED.getCode().equals(booking.getStatusCode())) {
            return CANCELLED;
        }
        if (booking.getCheckOut() != null && booking.getCheckOut().isBefore(today)) {
            return COMPLETED;
        }
        return UPCOMING;
    }
}
