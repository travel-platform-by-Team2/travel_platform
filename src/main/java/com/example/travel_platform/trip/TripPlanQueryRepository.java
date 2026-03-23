package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TripPlanQueryRepository {

    private final EntityManager em;

    public Optional<TripPlan> findPlan(Integer planId) {
        TripPlan tripPlan = em.find(TripPlan.class, planId);
        return Optional.ofNullable(tripPlan);
    }

    public Optional<TripPlan> findPlanWithPlaces(Integer planId) {
        return em.createQuery("""
                select distinct tp
                from TripPlan tp
                left join fetch tp.places
                where tp.id = :planId
                """, TripPlan.class)
                .setParameter("planId", planId)
                .getResultStream()
                .findFirst();
    }

    public List<TripPlan> findPlanList(Integer userId, int offset, int size) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                order by tp.startDate desc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countPlanList(Integer userId) {
        return em.createQuery("""
                select count(tp)
                from TripPlan tp
                where tp.user.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public List<TripPlan> findUpcomingPlanList(Integer userId, LocalDate today, int offset, int size) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate > :today
                order by tp.startDate asc, tp.id asc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countUpcomingPlanList(Integer userId, LocalDate today) {
        return em.createQuery("""
                select count(tp)
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate > :today
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .getSingleResult();
    }

    public List<TripPlan> findPastPlanList(Integer userId, LocalDate today, int offset, int size) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate <= :today
                order by tp.startDate asc, tp.id asc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countPastPlanList(Integer userId, LocalDate today) {
        return em.createQuery("""
                select count(tp)
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate <= :today
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .getSingleResult();
    }

    public Optional<TripPlan> findByDates(Integer userId, LocalDate startDate, LocalDate endDate) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate = :startDate
                  and tp.endDate = :endDate
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultStream()
                .findFirst();
    }
}
