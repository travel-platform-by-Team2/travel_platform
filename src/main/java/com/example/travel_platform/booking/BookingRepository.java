package com.example.travel_platform.booking;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

    private final EntityManager em;

    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            em.persist(booking);
            return booking;
        }
        return em.merge(booking);
    }

    public Optional<Booking> findById(Integer bookingId) {
        return Optional.ofNullable(em.find(Booking.class, bookingId));
    }

    @Transactional
    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from Booking b
                where b.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Transactional
    public int deleteByTripPlanUserId(Integer userId) {
        return em.createQuery("""
                delete from Booking b
                where b.tripPlan.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
