package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CalendarEventTypeTest {

    @Test
    void code() {
        assertEquals(CalendarEventType.TRIP, CalendarEventType.fromCode("TRIP"));
        assertEquals(CalendarEventType.BOOKING, CalendarEventType.fromCode("booking"));
        assertEquals(CalendarEventType.HOTEL, CalendarEventType.fromCode("HOTEL"));
    }

    @Test
    void nullCode() {
        assertNull(CalendarEventType.fromCodeOrNull(null));
        assertNull(CalendarEventType.fromCodeOrNull(""));
    }

    @Test
    void badCode() {
        assertThrows(IllegalArgumentException.class, () -> CalendarEventType.fromCode("UNKNOWN"));
    }
}
