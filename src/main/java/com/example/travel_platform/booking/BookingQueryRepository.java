package com.example.travel_platform.booking;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookingQueryRepository {

    private final EntityManager em;

    public List<Booking> findOwnedBookingList(Integer userId) {
        return em.createQuery("""
                select b
                from Booking b
                where b.user.id = :userId
                order by b.createdAt desc, b.id desc
                """, Booking.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Optional<Booking> findOwnedBooking(Integer userId, Integer bookingId) {
        return em.createQuery("""
                select b
                from Booking b
                join fetch b.tripPlan tp
                where b.user.id = :userId
                  and b.id = :bookingId
                """, Booking.class)
                .setParameter("userId", userId)
                .setParameter("bookingId", bookingId)
                .getResultStream()
                .findFirst();
    }
}
