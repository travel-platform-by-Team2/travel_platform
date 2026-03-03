package com.example.travel_platform.calendar;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;

    @Transactional
    public void createEvent(Integer sessionUserId, CalendarRequest.CreateEventDTO reqDTO) {
        // TODO: 시간 범위 유효성(startAt <= endAt) 검증
        // TODO: 엔티티 변환 후 저장
    }

    @Transactional
    public void updateEvent(Integer sessionUserId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        // TODO: 소유권 검증
        // TODO: 수정 처리
    }

    @Transactional
    public void deleteEvent(Integer sessionUserId, Integer eventId) {
        // TODO: 소유권 검증
        // TODO: 삭제 처리
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId) {
        // TODO: 사용자 일정 목록 조회
        // TODO: EventDTO 매핑
        return List.of();
    }
}

