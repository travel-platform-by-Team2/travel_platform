package com.example.travel_platform.calendar;

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

    public void delete(CalendarEvent event) {
        em.remove(event);
    }

    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from CalendarEvent e
                where e.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public int deleteByTripPlanUserId(Integer userId) {
        return em.createQuery("""
                delete from CalendarEvent e
                where e.tripPlan.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
