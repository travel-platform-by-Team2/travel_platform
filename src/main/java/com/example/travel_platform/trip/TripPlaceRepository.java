package com.example.travel_platform.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TripPlaceRepository {

    private final EntityManager em;

    public TripPlace save(TripPlace tripPlace) {
        if (tripPlace.getId() == null) {
            em.persist(tripPlace);
            return tripPlace;
        }
        return em.merge(tripPlace);
    }

    public long countByTripPlanId(Integer tripPlanId) {
        Long count = em.createQuery("""
                select count(tp)
                from TripPlace tp
                where tp.tripPlan.id = :tripPlanId
                """, Long.class)
                .setParameter("tripPlanId", tripPlanId)
                .getSingleResult();
        return count == null ? 0L : count;
    }

    public Map<Integer, Long> countByTripPlanIds(List<Integer> tripPlanIds) {
        if (tripPlanIds == null || tripPlanIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery("""
                select tp.tripPlan.id, count(tp)
                from TripPlace tp
                where tp.tripPlan.id in :tripPlanIds
                group by tp.tripPlan.id
                """, Object[].class)
                .setParameter("tripPlanIds", tripPlanIds)
                .getResultList();

        Map<Integer, Long> placeCounts = new HashMap<>();
        for (Object[] row : rows) {
            placeCounts.put((Integer) row[0], (Long) row[1]);
        }
        return placeCounts;
    }

    @Transactional
    public int deleteByTripPlanUserId(Integer userId) {
        return em.createQuery("""
                delete from TripPlace tp
                where tp.tripPlan.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
