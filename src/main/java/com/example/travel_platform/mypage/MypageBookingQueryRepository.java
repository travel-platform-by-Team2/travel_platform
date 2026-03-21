package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.booking.Booking;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MypageBookingQueryRepository {

    private final EntityManager em;

    public List<Booking> findUpcomingBookings(Integer userId, LocalDate today, int limit) {
        return em.createQuery("""
                select b
                from Booking b
                where b.user.id = :userId
                  and b.checkIn >= :today
                order by b.checkIn asc, b.id asc
                """, Booking.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setMaxResults(limit)
                .getResultList();
    }
}
