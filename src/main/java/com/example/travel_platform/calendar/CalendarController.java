package com.example.travel_platform.calendar;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private static final String CALENDAR_VIEW = "pages/calendar";
    private static final String MODEL = "model";

    @GetMapping
    public String calendarPage(Model model) {
        model.addAttribute(MODEL, CalendarResponse.CalendarPageDTO.createCalendarPage());
        return CALENDAR_VIEW;
    }
}
