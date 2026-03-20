package com.example.travel_platform.calendar;

import java.time.LocalDate;
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
        if (reqDTO.getStartAt() != null && reqDTO.getEndAt() != null
                && reqDTO.getStartAt().isAfter(reqDTO.getEndAt())) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));

        CalendarEvent event = new CalendarEvent();
        event.setUser(user);
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());
        event.setMemo(reqDTO.getMemo());

        CalendarEvent savedEvent = calendarRepository.save(event);

        return toEventDTO(savedEvent);
    }

    @Transactional
    public CalendarResponse.EventDTO updateEvent(Integer userId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        if (reqDTO.getStartAt() != null && reqDTO.getEndAt() != null
                && reqDTO.getStartAt().isAfter(reqDTO.getEndAt())) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        CalendarEvent event = calendarRepository.findById(eventId)
                .orElseThrow(() -> new Exception400("일정을 찾을 수 없습니다."));

        if (event.getUser() == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));
            event.setUser(user);
        }

        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());
        event.setMemo(reqDTO.getMemo());

        CalendarEvent updatedEvent = calendarRepository.update(event);
        return toEventDTO(updatedEvent);
    }

    @Transactional
    public Map<String, Integer> deleteEvent(Integer eventId) {
        CalendarEvent event = calendarRepository.findById(eventId)
                .orElseThrow(() -> new Exception400("일정을 찾을 수 없습니다."));

        calendarRepository.delete(event);
        return Map.of("eventId", eventId);
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        List<CalendarEvent> events = calendarRepository.findEventListByUserId(sessionUserId, startDate, endDate);

        return events.stream()
                .map(event -> toEventDTO(event))
                .toList();
    }

    public List<CalendarResponse.DayNodeDTO> getDayNodeList(Integer sessionUserId, Integer year, Integer month) {
        return List.of();
    }

    public CalendarResponse.DayNodeDTO getDayNode(Integer sessionUserId, LocalDate date) {
        return null;
    }

    private CalendarResponse.EventDTO toEventDTO(CalendarEvent event) {
        return CalendarResponse.EventDTO.builder()
                .id(event.getId())
                .tripPlanId(event.getTripPlan() == null ? null : event.getTripPlan().getId())
                .title(event.getTitle())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .eventType(event.getEventType())
                .memo(event.getMemo())
                .build();
    }
}
