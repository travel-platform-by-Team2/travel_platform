package com.example.travel_platform.booking;

public enum BookingStatus {
    BOOKED("booked", "예약 확정"),
    CANCELLED("cancelled", "예약 취소");

    private final String code;
    private final String label;

    BookingStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean canCancel() {
        return this == BOOKED;
    }
}
