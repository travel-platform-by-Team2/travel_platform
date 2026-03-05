package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CalendarRepository {

    private final EntityManager em;

    public CalendarEvent save(CalendarEvent event) {
        // TODO: 일정 저장 리팩토링 뼈대 유지
        return event;
    }

    public Optional<CalendarEvent> findById(Integer eventId) {
        // TODO: 일정 단건 조회 리팩토링 뼈대 유지
        return Optional.empty();
    }

    public List<CalendarEvent> findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate) {
        // TODO: 기간 기반 일정 목록 조회 리팩토링 뼈대 유지
        return List.of();
    }

    public List<CalendarEvent> findEventListByMonth(Integer userId, Integer year, Integer month) {
        // TODO: 월 기반 일자 노드 조회 리팩토링 뼈대 유지
        return List.of();
    }

    public List<CalendarEvent> findEventListByDate(Integer userId, LocalDate date) {
        // TODO: 단일 일자 노드 조회 리팩토링 뼈대 유지
        return List.of();
    }
}
