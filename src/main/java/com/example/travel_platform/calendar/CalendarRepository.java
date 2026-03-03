package com.example.travel_platform.calendar;

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
        // TODO: 일정 저장 처리
        return event;
    }

    public Optional<CalendarEvent> findById(Integer eventId) {
        // TODO: 일정 단건 조회 구현
        return Optional.empty();
    }

    public List<CalendarEvent> findListByUserId(Integer userId) {
        // TODO: 사용자 일정 목록 조회(기간 조건 포함) 구현
        return List.of();
    }
}

