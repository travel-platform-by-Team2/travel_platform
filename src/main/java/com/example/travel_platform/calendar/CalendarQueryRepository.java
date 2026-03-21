package com.example.travel_platform.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CalendarQueryRepository {

    private final EntityManager em;

    public List<CalendarEvent> findEventList(Integer userId, LocalDate startDate, LocalDate endDate) {
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
}
