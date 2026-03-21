package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.booking.BookingStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MypageQueryRepository {

    private final EntityManager em;

    public List<BookingSummaryRow> findUpcomingBookingRows(Integer userId, LocalDate today, int limit) {
        return em.createQuery("""
                select
                    b.id as bookingId,
                    b.lodgingName as lodgingName,
                    b.checkIn as checkIn,
                    b.checkOut as checkOut
                from Booking b
                where b.user.id = :userId
                  and b.checkIn >= :today
                  and b.status = :status
                order by b.checkIn asc, b.id asc
                """, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setParameter("status", BookingStatus.BOOKED)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(this::toBookingSummaryRow)
                .toList();
    }

    public List<TripPlanSummaryRow> findUpcomingTripPlanRows(Integer userId, LocalDate today, int limit) {
        return em.createQuery("""
                select
                    tp.id as planId,
                    tp.title as title,
                    tp.startDate as startDate,
                    tp.endDate as endDate
                from TripPlan tp
                where tp.user.id = :userId
                  and tp.startDate > :today
                order by tp.startDate asc, tp.id asc
                """, Tuple.class)
                .setParameter("userId", userId)
                .setParameter("today", today)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(this::toTripPlanSummaryRow)
                .toList();
    }

    private BookingSummaryRow toBookingSummaryRow(Tuple tuple) {
        return new BookingSummaryRow(
                tuple.get("bookingId", Integer.class),
                tuple.get("lodgingName", String.class),
                tuple.get("checkIn", LocalDate.class),
                tuple.get("checkOut", LocalDate.class));
    }

    private TripPlanSummaryRow toTripPlanSummaryRow(Tuple tuple) {
        return new TripPlanSummaryRow(
                tuple.get("planId", Integer.class),
                tuple.get("title", String.class),
                tuple.get("startDate", LocalDate.class),
                tuple.get("endDate", LocalDate.class));
    }
}
