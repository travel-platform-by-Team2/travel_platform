package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MypageBookingQueryRepository {

    private final EntityManager em;

    public List<MypageBookingSummaryRow> findUpcomingBookingSummaryRows(Integer userId, LocalDate today, int limit) {
        return em.createQuery("""
                select new com.example.travel_platform.mypage.MypageBookingSummaryRow(
                    b.id,
                    b.lodgingName,
                    b.checkIn,
                    b.checkOut
                )
                from Booking b
                where b.user.id = :userId
                  and b.checkIn >= :today
                order by b.checkIn asc, b.id asc
                """, MypageBookingSummaryRow.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setMaxResults(limit)
                .getResultList();
    }

    public boolean existsOwnedBooking(Integer userId, Integer bookingId) {
        Long count = em.createQuery("""
                select count(b)
                from Booking b
                where b.user.id = :userId
                  and b.id = :bookingId
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("bookingId", bookingId)
                .getSingleResult();

        return count != null && count > 0;
    }
}
