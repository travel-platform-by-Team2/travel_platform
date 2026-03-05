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
        // TODO: 일정 생성 비즈니스 로직 구현
    }

    @Transactional
    public void updateEvent(Integer sessionUserId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        // TODO: 일정 수정 비즈니스 로직 구현
    }

    @Transactional
    public void deleteEvent(Integer sessionUserId, Integer eventId) {
        // TODO: 일정 삭제 비즈니스 로직 구현
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new Exception400("startDate는 endDate보다 늦을 수 없습니다.");
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
        // TODO: 월 단위 날짜 노드 조회 비즈니스 로직 구현
        return List.of();
    }

    public CalendarResponse.DayNodeDTO getDayNode(Integer sessionUserId, LocalDate date) {
        // TODO: 단일 날짜 노드 조회 비즈니스 로직 구현
        return null;
    }
}
