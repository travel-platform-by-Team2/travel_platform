package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    @Transactional
    public void createEvent(Integer sessionUserId, CalendarRequest.CreateEventDTO reqDTO) {
        if (reqDTO.getStartAt() != null && reqDTO.getEndAt() != null
                && reqDTO.getStartAt().isAfter(reqDTO.getEndAt())) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        CalendarEvent event = new CalendarEvent();
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());

        calendarRepository.save(event);
    }

    @Transactional
    public void updateEvent(Integer sessionUserId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        if (reqDTO.getStartAt() != null && reqDTO.getEndAt() != null
                && reqDTO.getStartAt().isAfter(reqDTO.getEndAt())) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        CalendarEvent event = calendarRepository.findById(eventId)
                .orElseThrow(() -> new Exception400("일정을 찾을 수 없습니다."));

        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(reqDTO.getEventType());

        calendarRepository.update(event);
    }

    @Transactional
    public void deleteEvent(Integer sessionUserId, Integer eventId) {
        // TODO: 일정 삭제 비즈니스 로직 구현
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new Exception400("시작일은 종료일 보다 늦을 수 없습니다.");
        }

        List<CalendarEvent> events = calendarRepository.findEventListByUserId(sessionUserId, startDate, endDate);

        return events.stream()
                .map(event -> CalendarResponse.EventDTO.builder()
                        .id(event.getId())
                        .tripPlanId(event.getTripPlan() == null ? null : event.getTripPlan().getId())
                        .title(event.getTitle())
                        .startAt(event.getStartAt())
                        .endAt(event.getEndAt())
                        .eventType(event.getEventType())
                        .build())
                .toList();
    }

    public List<CalendarResponse.DayNodeDTO> getDayNodeList(Integer sessionUserId, Integer year, Integer month) {
        return List.of();
    }

    public CalendarResponse.DayNodeDTO getDayNode(Integer sessionUserId, LocalDate date) {
        return null;
    }
}
