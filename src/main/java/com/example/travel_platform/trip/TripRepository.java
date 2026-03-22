package com.example.travel_platform.trip;

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

    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from TripPlan tp
                where tp.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
