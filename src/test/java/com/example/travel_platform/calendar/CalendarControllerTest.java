package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class CalendarControllerTest {

    @Test
    void page() {
        CalendarController controller = new CalendarController();
        Model model = new ExtendedModelMap();

        assertEquals("pages/calendar", controller.calendarPage(model));
        assertEquals("TravelMate | 캘린더",
                ((CalendarResponse.CalendarPageDTO) model.getAttribute("model")).getPageTitle());
    }
}
