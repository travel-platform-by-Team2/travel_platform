package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.util.Resp;
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
    public ResponseEntity<?> createEvent(
            @RequestBody CalendarRequest.CreateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        CalendarResponse.EventDTO responseDTO = calendarService.createEvent(userId, reqDTO);
        return Resp.ok(responseDTO);
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable("eventId") Integer eventId,
            @RequestBody CalendarRequest.UpdateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        CalendarResponse.EventDTO responseDTO = calendarService.updateEvent(userId, eventId, reqDTO);
        return Resp.ok(responseDTO);
    }

    @PostMapping("/delete/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable("eventId") Integer eventId) {
        Map<String, Integer> responseDTO = calendarService.deleteEvent(eventId);
        return Resp.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<?> getCalendar(@RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "date", required = false) LocalDate date) {

        Integer sessionUserId = resolveUserId();

        if (date != null) {
            CalendarResponse.DayNodeDTO dayNode = calendarService.getDayNode(sessionUserId, date);
            return Resp.ok(dayNode);
        }
        if (year != null && month != null) {
            List<CalendarResponse.DayNodeDTO> dayNodes = calendarService.getDayNodeList(sessionUserId, year, month);
            return Resp.ok(dayNodes);
        }
        List<CalendarResponse.EventDTO> events = calendarService.getEventList(sessionUserId, startDate, endDate);
        return Resp.ok(events);
    }

    private Integer resolveUserId() {
        Object sessionUser = session.getAttribute("sessionUser");
        if (sessionUser instanceof User user) {
            return user.getId();
        }
        throw new Exception400("로그인이 필요합니다.");
    }
}