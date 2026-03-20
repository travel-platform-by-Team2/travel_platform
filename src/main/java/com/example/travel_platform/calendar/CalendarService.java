package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;

    @Transactional
    public CalendarResponse.EventDTO createEvent(Integer userId, CalendarRequest.CreateEventDTO reqDTO) {
        validateEventRange(reqDTO.getStartAt(), reqDTO.getEndAt());

        User user = findUser(userId);
        CalendarEvent savedEvent = calendarRepository.save(buildCreatedEvent(user, reqDTO));
        return CalendarResponse.EventDTO.from(savedEvent);
    }

    @Transactional
    public CalendarResponse.EventDTO updateEvent(Integer userId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        validateEventRange(reqDTO.getStartAt(), reqDTO.getEndAt());

        CalendarEvent event = findEvent(eventId);
        attachUserIfMissing(userId, event);
        applyEventChanges(event, reqDTO);

        CalendarEvent updatedEvent = calendarRepository.update(event);
        return CalendarResponse.EventDTO.from(updatedEvent);
    }

    @Transactional
    public Map<String, Integer> deleteEvent(Integer eventId) {
        CalendarEvent event = findEvent(eventId);
        calendarRepository.delete(event);
        return Map.of("eventId", eventId);
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<CalendarEvent> events = calendarRepository.findEventListByUserId(sessionUserId, startDate, endDate);

        return events.stream()
                .map(CalendarResponse.EventDTO::from)
                .toList();
    }

    public List<CalendarResponse.DayNodeDTO> getDayNodeList(Integer sessionUserId, Integer year, Integer month) {
        return buildPlaceholderDayNodeList(sessionUserId, year, month);
    }

    public CalendarResponse.DayNodeDTO getDayNode(Integer sessionUserId, LocalDate date) {
        return buildPlaceholderDayNode(sessionUserId, date);
    }

    private void validateEventRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));
    }

    private CalendarEvent findEvent(Integer eventId) {
        return calendarRepository.findById(eventId)
                .orElseThrow(() -> new Exception400("일정을 찾을 수 없습니다."));
    }

    private CalendarEvent buildCreatedEvent(User user, CalendarRequest.CreateEventDTO reqDTO) {
        CalendarEvent event = new CalendarEvent();
        event.setUser(user);
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());
        event.setMemo(reqDTO.getMemo());
        return event;
    }

    private void attachUserIfMissing(Integer userId, CalendarEvent event) {
        if (event.getUser() == null) {
            event.setUser(findUser(userId));
        }
    }

    private void applyEventChanges(CalendarEvent event, CalendarRequest.UpdateEventDTO reqDTO) {
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());
        event.setMemo(reqDTO.getMemo());
    }

    private List<CalendarResponse.DayNodeDTO> buildPlaceholderDayNodeList(
            Integer sessionUserId,
            Integer year,
            Integer month) {
        if (sessionUserId == null || year == null || month == null) {
            return List.of();
        }
        return List.of();
    }

    private CalendarResponse.DayNodeDTO buildPlaceholderDayNode(Integer sessionUserId, LocalDate date) {
        if (sessionUserId == null || date == null) {
            return null;
        }
        return null;
    }
}
