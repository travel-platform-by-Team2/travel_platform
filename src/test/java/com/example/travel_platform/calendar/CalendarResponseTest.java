package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;

class CalendarResponseTest {

    @Test
    void event() {
        User user = user(1);
        TripPlan tripPlan = TripPlan.create(
                user,
                "제주 여행",
                "jeju",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                "");
        tripPlan.setId(31);

        CalendarEvent event = new CalendarEvent();
        event.setId(41);
        event.setTripPlan(tripPlan);
        event.setTitle("제주 출발");
        event.setStartAt(LocalDateTime.of(2026, 4, 10, 8, 0));
        event.setEndAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        event.setEventType(CalendarEventType.BOOKING);
        event.setMemo("숙소 체크인");

        CalendarResponse.EventDTO dto = CalendarResponse.EventDTO.fromCalendarEvent(event);

        assertEquals(41, dto.getId());
        assertEquals(31, dto.getTripPlanId());
        assertEquals("BOOKING", dto.getEventType());
        assertEquals("숙소 체크인", dto.getMemo());
    }

    private User user(Integer id) {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(id);
        return user;
    }
}
