package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MypageTripPlanQueryRepository {

    private final EntityManager em;

    public List<MypageTripPlanSummaryRow> findUpcomingTripPlanSummaryRows(Integer userId, LocalDate today, int limit) {
        return em.createQuery("""
                select new com.example.travel_platform.mypage.MypageTripPlanSummaryRow(
                    tp.id,
                    tp.title,
                    tp.startDate,
                    tp.endDate
                )
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate > :today
                order by tp.startDate asc, tp.id asc
                """, MypageTripPlanSummaryRow.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setMaxResults(limit)
                .getResultList();
    }
}
