package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class CalendarApiControllerTest {

    @Test
    void create() {
        CalendarService calendarService = mock(CalendarService.class);
        CalendarApiController controller = new CalendarApiController(calendarService, session(9));
        CalendarRequest.CreateEventDTO reqDTO = new CalendarRequest.CreateEventDTO();
        CalendarResponse.EventDTO dto = event(1, "일정");

        when(calendarService.createEvent(9, reqDTO)).thenReturn(dto);

        ResponseEntity<Resp<CalendarResponse.EventDTO>> response = controller.createEvent(reqDTO);

        verify(calendarService).createEvent(9, reqDTO);
        assertSame(dto, response.getBody().getBody());
    }

    @Test
    void update() {
        CalendarService calendarService = mock(CalendarService.class);
        CalendarApiController controller = new CalendarApiController(calendarService, session(9));
        CalendarRequest.UpdateEventDTO reqDTO = new CalendarRequest.UpdateEventDTO();
        CalendarResponse.EventDTO dto = event(2, "수정");

        when(calendarService.updateEvent(9, 22, reqDTO)).thenReturn(dto);

        ResponseEntity<Resp<CalendarResponse.EventDTO>> response = controller.updateEvent(22, reqDTO);

        verify(calendarService).updateEvent(9, 22, reqDTO);
        assertSame(dto, response.getBody().getBody());
    }

    @Test
    void del() {
        CalendarService calendarService = mock(CalendarService.class);
        CalendarApiController controller = new CalendarApiController(calendarService, session(9));
        CalendarResponse.DeleteResultDTO dto = CalendarResponse.DeleteResultDTO.createDeleteResult(33);

        when(calendarService.deleteEvent(9, 33)).thenReturn(dto);

        ResponseEntity<Resp<CalendarResponse.DeleteResultDTO>> response = controller.deleteEvent(33);

        verify(calendarService).deleteEvent(9, 33);
        assertSame(dto, response.getBody().getBody());
    }

    @Test
    void range() {
        CalendarService calendarService = mock(CalendarService.class);
        CalendarApiController controller = new CalendarApiController(calendarService, session(9));
        List<CalendarResponse.EventDTO> dto = List.of(event(1, "여행"));

        when(calendarService.getEventList(9, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30))).thenReturn(dto);

        ResponseEntity<Resp<List<CalendarResponse.EventDTO>>> response = controller.getCalendar(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));

        verify(calendarService).getEventList(9, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        assertSame(dto, response.getBody().getBody());
    }

    @Test
    void login() {
        CalendarService calendarService = mock(CalendarService.class);
        CalendarApiController controller = new CalendarApiController(calendarService, new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.getCalendar(null, null));
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }

    private CalendarResponse.EventDTO event(Integer id, String title) {
        return CalendarResponse.EventDTO.builder()
                .id(id)
                .title(title)
                .startAt(LocalDateTime.of(2026, 4, 10, 10, 0))
                .endAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                .eventType("TRIP")
                .memo("memo")
                .build();
    }
}
