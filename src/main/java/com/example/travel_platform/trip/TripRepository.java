package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TripRepository {

    private final EntityManager em;

    public TripPlan savePlan(TripPlan tripPlan) {
        if (tripPlan.getId() == null) {
            em.persist(tripPlan);
        } else {
            em.merge(tripPlan);
        }
        return tripPlan;
    }

    public TripPlace savePlace(TripPlace tripPlace) {
        // TODO: 장소 저장 처리
        return tripPlace;
    }

    public Optional<TripPlan> findPlanById(Integer planId) {
        // TODO: planId 기준 조회 쿼리 구현

        TripPlan tripPlan = em.find(TripPlan.class, planId);
        return Optional.ofNullable(tripPlan);
    }

    // 전체 여행 목록 조회 (시작일을 기준으로 정렬)
    public List<TripPlan> findPlanListByUserId(Integer userId, int offset, int size) {
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

    // 전체 여행 개수
    public Long countPlanByUserId(Integer userId) {
        return em.createQuery("""
                select count(tp)
                from TripPlan tp
                where tp.user.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    // 예정된 여행 목록 조회
    public List<TripPlan> findUpcomingPlanListByUserId(Integer userId, LocalDate today, int offset, int size) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                and tp.startDate > :today
                order by tp.startDate asc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    // 예정된 여행 개수
    public Long countUpcomingPlanByUserId(Integer userId, LocalDate today) {
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

    // 지난 여행 목록 조회
    public List<TripPlan> findPastPlanListByUserId(Integer userId, LocalDate today, int offset, int size) {
        return em.createQuery("""
                select tp
                from TripPlan tp
                where tp.user.id = :userId
                and tp.startDate <= :today
                order by tp.startDate asc
                """, TripPlan.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    // 지난 여행 개수
    public Long countPastPlanByUserId(Integer userId, LocalDate today) {
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
}
