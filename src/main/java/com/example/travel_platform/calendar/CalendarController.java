package com.example.travel_platform.calendar;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calendar/events")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping
    public void createEvent(@Valid @RequestBody CalendarRequest.CreateEventDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        calendarService.createEvent(1, reqDTO);
    }

    @PutMapping("/{eventId}")
    public void updateEvent(@PathVariable Integer eventId, @Valid @RequestBody CalendarRequest.UpdateEventDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        calendarService.updateEvent(1, eventId, reqDTO);
    }

    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Integer eventId) {
        // TODO: 세션 사용자 식별값 연동
        calendarService.deleteEvent(1, eventId);
    }

    @GetMapping
    public Object getEventList() {
        // TODO: 기간 파라미터(start/end) 연동
        return calendarService.getEventList(1);
    }
}

