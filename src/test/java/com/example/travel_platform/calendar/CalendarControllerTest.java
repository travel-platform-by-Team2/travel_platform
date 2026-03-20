package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CalendarControllerTest {

    @Test
    void page() {
        CalendarController controller = new CalendarController();

        assertEquals("pages/calendar", controller.calendarPage());
    }
}
