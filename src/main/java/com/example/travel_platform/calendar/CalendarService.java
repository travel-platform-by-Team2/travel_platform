package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripPlanQueryRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final CalendarQueryRepository calendarQueryRepository;
    private final TripPlanQueryRepository tripPlanQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @Transactional
    public CalendarResponse.EventDTO createEvent(Integer userId, CalendarRequest.CreateEventDTO reqDTO) {
        validateEventRange(reqDTO.getStartAt(), reqDTO.getEndAt());

        User user = findUser(userId);
        CalendarEvent savedEvent = calendarRepository.save(buildCreatedEvent(user, reqDTO));
        return CalendarResponse.EventDTO.fromCalendarEvent(savedEvent);
    }

    @Transactional
    public CalendarResponse.EventDTO updateEvent(Integer userId, Integer eventId, CalendarRequest.UpdateEventDTO reqDTO) {
        validateEventRange(reqDTO.getStartAt(), reqDTO.getEndAt());

        CalendarEvent event = findEvent(eventId);
        attachUserIfMissing(userId, event);
        validateEventOwner(userId, event);
        applyEventChanges(event, reqDTO);

        CalendarEvent updatedEvent = calendarRepository.update(event);
        return CalendarResponse.EventDTO.fromCalendarEvent(updatedEvent);
    }

    @Transactional
    public Map<String, Integer> deleteEvent(Integer userId, Integer eventId) {
        CalendarEvent event = findEvent(eventId);
        attachUserIfMissing(userId, event);
        validateEventOwner(userId, event);
        calendarRepository.delete(event);
        return Map.of("eventId", eventId);
    }

    public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<CalendarEvent> events = calendarQueryRepository.findEventList(sessionUserId, startDate, endDate);

        return events.stream()
                .map(CalendarResponse.EventDTO::fromCalendarEvent)
                .toList();
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
        return userQueryRepository.findUser(userId)
                .orElseThrow(() -> new Exception400("사용자를 찾을 수 없습니다."));
    }

    private CalendarEvent findEvent(Integer eventId) {
        return calendarRepository.findById(eventId)
                .orElseThrow(() -> new Exception400("일정을 찾을 수 없습니다."));
    }

    private CalendarEvent buildCreatedEvent(User user, CalendarRequest.CreateEventDTO reqDTO) {
        CalendarEvent event = new CalendarEvent();
        event.setUser(user);
        event.setTripPlan(findTripPlan(reqDTO.getTripPlanId()));
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(resolveEventType(reqDTO.getEventType()));
        event.setMemo(reqDTO.getMemo());
        return event;
    }

    private void attachUserIfMissing(Integer userId, CalendarEvent event) {
        if (event.getUser() == null) {
            event.setUser(findUser(userId));
        }
    }

    private void validateEventOwner(Integer userId, CalendarEvent event) {
        if (!event.isOwnedBy(userId)) {
            throw new Exception403("본인 일정만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private void applyEventChanges(CalendarEvent event, CalendarRequest.UpdateEventDTO reqDTO) {
        event.setTitle(reqDTO.getTitle());
        event.setStartAt(reqDTO.getStartAt());
        event.setEndAt(reqDTO.getEndAt());
        event.setEventType(resolveEventType(reqDTO.getEventType()));
        event.setMemo(reqDTO.getMemo());
    }

    private TripPlan findTripPlan(Integer tripPlanId) {
        if (tripPlanId == null) {
            return null;
        }
        return tripPlanQueryRepository.findPlan(tripPlanId).orElse(null);
    }

    private CalendarEventType resolveEventType(String eventTypeCode) {
        String safeEventTypeCode = eventTypeCode == null || eventTypeCode.isBlank()
                ? CalendarEventType.TRIP.getCode()
                : eventTypeCode;
        try {
            return CalendarEventType.fromCode(safeEventTypeCode);
        } catch (IllegalArgumentException e) {
            throw new Exception400("지원하지 않는 일정 타입입니다.");
        }
    }
}
