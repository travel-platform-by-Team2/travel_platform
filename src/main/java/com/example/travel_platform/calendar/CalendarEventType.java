package com.example.travel_platform.calendar;

import java.util.Arrays;

public enum CalendarEventType {
    TRIP("TRIP"),
    PERSONAL("PERSONAL"),
    FAMILY("FAMILY"),
    WORK("WORK"),
    BOOKING("BOOKING"),
    HOTEL("HOTEL");

    private final String code;

    CalendarEventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CalendarEventType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown calendar event type: " + code));
    }

    public static CalendarEventType fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
