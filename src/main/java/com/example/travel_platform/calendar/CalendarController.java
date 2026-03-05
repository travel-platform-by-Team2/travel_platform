package com.example.travel_platform.calendar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final HttpSession session;

    @GetMapping
    public String calendarPage() {
        return "pages/calendar";
    }
}
