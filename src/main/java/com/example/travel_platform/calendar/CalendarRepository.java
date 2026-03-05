package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        em.persist(event);
        return event;
    }

    public CalendarEvent update(CalendarEvent event) {
        return em.merge(event);
    }

    public Optional<CalendarEvent> findById(Integer eventId) {
        CalendarEvent event = em.find(CalendarEvent.class, eventId);
        return Optional.ofNullable(event);
    }

    public List<CalendarEvent> findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);

            return em.createQuery("""
                    select e
                    from CalendarEvent e
                    where e.user.id = :userId
                      and e.startAt <= :endDateTime
                      and e.endAt >= :startDateTime
                    order by e.startAt asc
                    """, CalendarEvent.class)
                    .setParameter("userId", userId)
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .getResultList();
        }

        return em.createQuery("""
                select e
                from CalendarEvent e
                where e.user.id = :userId
                order by e.startAt asc
                """, CalendarEvent.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<CalendarEvent> findEventListByMonth(Integer userId, Integer year, Integer month) {
        // TODO: 월 기반 날짜 노드 조회 로직 구현
        return List.of();
    }

    public List<CalendarEvent> findEventListByDate(Integer userId, LocalDate date) {
        // TODO: 단일 날짜 노드 조회 로직 구현
        return List.of();
    }
}
