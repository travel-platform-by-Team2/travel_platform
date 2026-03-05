package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    @Transactional
    public void createEvent(Integer sessionUserId, CalendarRequest.CreateEventDTO reqDTO) {
        // TODO: 일정 생성 리팩토링 뼈대 유지
    }

    @Transactional
    public void updateEvent(Integer sessionUserId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        // TODO: 일정 수정 리팩토링 뼈대 유지
    }

    @Transactional
    public void deleteEvent(Integer sessionUserId, Integer eventId) {
        // TODO: 일정 삭제 리팩토링 뼈대 유지
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        // TODO: 기간 기반 일정 조회 리팩토링 뼈대 유지
        return List.of();
    }

    public List<CalendarResponse.DayNodeDTO> getDayNodeList(Integer sessionUserId, Integer year, Integer month) {
        // TODO: 월 단위 일자 노드 조회 리팩토링 뼈대 유지
        return List.of();
    }

    public CalendarResponse.DayNodeDTO getDayNode(Integer sessionUserId, LocalDate date) {
        // TODO: 단일 일자 노드 조회 리팩토링 뼈대 유지
        return null;
    }
}
