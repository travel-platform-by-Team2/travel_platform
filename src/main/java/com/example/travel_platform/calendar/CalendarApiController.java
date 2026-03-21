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

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final CalendarService calendarService;
    private final HttpSession session;

    @PostMapping("/create")
    public ResponseEntity<Resp<CalendarResponse.EventDTO>> createEvent(
            @RequestBody CalendarRequest.CreateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        CalendarResponse.EventDTO responseDTO = calendarService.createEvent(userId, reqDTO);
        return Resp.ok(responseDTO);
    }

    @PutMapping("/update/{eventId}")
    public ResponseEntity<Resp<CalendarResponse.EventDTO>> updateEvent(@PathVariable(name = "eventId") Integer eventId,
            @RequestBody CalendarRequest.UpdateEventDTO reqDTO) {
        Integer userId = resolveUserId();
        CalendarResponse.EventDTO responseDTO = calendarService.updateEvent(userId, eventId, reqDTO);
        return Resp.ok(responseDTO);
    }

    @PostMapping("/delete/{eventId}")
    public ResponseEntity<Resp<Map<String, Integer>>> deleteEvent(@PathVariable(name = "eventId") Integer eventId) {
        Map<String, Integer> responseDTO = calendarService.deleteEvent(resolveUserId(), eventId);
        return Resp.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<?> getCalendar(
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "date", required = false) LocalDate date) {
        Integer sessionUserId = resolveUserId();
        return getCalendarResponse(sessionUserId, startDate, endDate, year, month, date);
    }

    private Integer resolveUserId() {
        return SessionUsers.requireUserId(session);
    }

    private ResponseEntity<?> getCalendarResponse(
            Integer sessionUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer year,
            Integer month,
            LocalDate date) {
        if (date != null) {
            return Resp.ok(calendarService.getDayNode(sessionUserId, date));
        }
        if (hasMonthQuery(year, month)) {
            return Resp.ok(calendarService.getDayNodeList(sessionUserId, year, month));
        }
        return Resp.ok(calendarService.getEventList(sessionUserId, startDate, endDate));
    }

    private boolean hasMonthQuery(Integer year, Integer month) {
        return year != null && month != null;
    }
}
