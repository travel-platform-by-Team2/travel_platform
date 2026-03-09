package com.example.travel_platform.calendar;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.travel_platform.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final CalendarService calendarService;
    private final HttpSession session;

    @PostMapping("/create")
    public void createEvent(@RequestBody CalendarRequest.CreateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        calendarService.createEvent(userId, reqDTO);
    }

    @PutMapping("/update/{eventId}")
    public void updateEvent(@PathVariable Integer eventId, @RequestBody CalendarRequest.UpdateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        calendarService.updateEvent(userId, eventId, reqDTO);
    }

    @PostMapping("/delete/{eventId}")
    public void deleteEvent(@PathVariable Integer eventId) {
        calendarService.deleteEvent(eventId);
    }

    @GetMapping
    public Object getCalendar(@RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDate date) {

        Integer sessionUserId = resolveUserId();

        if (date != null) {
            return calendarService.getDayNode(sessionUserId, date);
        }
        if (year != null && month != null) {
            return calendarService.getDayNodeList(sessionUserId, year, month);
        }
        return calendarService.getEventList(sessionUserId, startDate, endDate);
    }

    private Integer resolveUserId() {
        Object sessionUser = session.getAttribute("sessionUser");
        if (sessionUser instanceof User user) {
            return user.getId();
        }
        return 1;
    }
}
