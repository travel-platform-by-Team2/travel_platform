package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

class CalendarServiceTest {

    @Test
    void create() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);
        User user = user(4);
        CalendarRequest.CreateEventDTO reqDTO = createReq();

        when(userRepository.findById(4)).thenReturn(Optional.of(user));
        when(calendarRepository.save(org.mockito.ArgumentMatchers.any(CalendarEvent.class)))
                .thenAnswer(invocation -> {
                    CalendarEvent event = invocation.getArgument(0);
                    event.setId(11);
                    return event;
                });

        CalendarResponse.EventDTO response = service.createEvent(4, reqDTO);

        ArgumentCaptor<CalendarEvent> captor = ArgumentCaptor.forClass(CalendarEvent.class);
        verify(calendarRepository).save(captor.capture());
        CalendarEvent saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertEquals("제주 여행", saved.getTitle());
        assertEquals(11, response.getId());
        assertEquals("제주 여행", response.getTitle());
    }

    @Test
    void updateUser() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);
        User user = user(7);
        CalendarEvent event = new CalendarEvent();
        event.setId(21);
        event.setTitle("old");
        event.setStartAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        event.setEndAt(LocalDateTime.of(2026, 5, 1, 11, 0));
        event.setEventType("TRIP");
        event.setMemo("old");
        CalendarRequest.UpdateEventDTO reqDTO = updateReq();

        when(calendarRepository.findById(21)).thenReturn(Optional.of(event));
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(calendarRepository.update(event)).thenReturn(event);

        CalendarResponse.EventDTO response = service.updateEvent(7, 21, reqDTO);

        verify(calendarRepository).update(event);
        assertSame(user, event.getUser());
        assertEquals("수정 일정", event.getTitle());
        assertEquals("수정 일정", response.getTitle());
    }

    @Test
    void del() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);
        CalendarEvent event = new CalendarEvent();
        event.setId(31);

        when(calendarRepository.findById(31)).thenReturn(Optional.of(event));

        Map<String, Integer> response = service.deleteEvent(31);

        verify(calendarRepository).delete(event);
        assertEquals(Map.of("eventId", 31), response);
    }

    @Test
    void range() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);
        CalendarEvent event = new CalendarEvent();
        TripPlan tripPlan = TripPlan.create(
                user(1),
                "제주 여행",
                "jeju",
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3),
                "");
        tripPlan.setId(101);
        event.setId(41);
        event.setTripPlan(tripPlan);
        event.setTitle("제주 일정");
        event.setStartAt(LocalDateTime.of(2026, 4, 2, 10, 0));
        event.setEndAt(LocalDateTime.of(2026, 4, 2, 12, 0));
        event.setEventType("TRIP");
        event.setMemo("memo");

        when(calendarRepository.findEventListByUserId(9, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(event));

        List<CalendarResponse.EventDTO> response = service.getEventList(
                9,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));

        assertEquals(1, response.size());
        assertEquals(101, response.get(0).getTripPlanId());
        assertEquals("제주 일정", response.get(0).getTitle());
    }

    @Test
    void monthPh() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);

        assertEquals(List.of(), service.getDayNodeList(9, 2026, 4));
    }

    @Test
    void dayPh() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);

        assertNull(service.getDayNode(9, LocalDate.of(2026, 4, 10)));
    }

    @Test
    void rangeErr() {
        CalendarRepository calendarRepository = mock(CalendarRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CalendarService service = new CalendarService(calendarRepository, userRepository);

        assertThrows(
                Exception400.class,
                () -> service.getEventList(9, LocalDate.of(2026, 4, 30), LocalDate.of(2026, 4, 1)));
        verifyNoInteractions(calendarRepository);
    }

    private CalendarRequest.CreateEventDTO createReq() {
        CalendarRequest.CreateEventDTO reqDTO = new CalendarRequest.CreateEventDTO();
        reqDTO.setTitle("제주 여행");
        reqDTO.setStartAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        reqDTO.setEndAt(LocalDateTime.of(2026, 4, 10, 12, 0));
        reqDTO.setEventType("TRIP");
        reqDTO.setMemo("memo");
        return reqDTO;
    }

    private CalendarRequest.UpdateEventDTO updateReq() {
        CalendarRequest.UpdateEventDTO reqDTO = new CalendarRequest.UpdateEventDTO();
        reqDTO.setTitle("수정 일정");
        reqDTO.setStartAt(LocalDateTime.of(2026, 5, 2, 10, 0));
        reqDTO.setEndAt(LocalDateTime.of(2026, 5, 2, 11, 0));
        reqDTO.setEventType("HOTEL");
        reqDTO.setMemo("new");
        return reqDTO;
    }

    private User user(Integer id) {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(id);
        return user;
    }
}
