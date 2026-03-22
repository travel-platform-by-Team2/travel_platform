package com.example.travel_platform.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {

    long countByTripPlanId(Integer tripPlanId);

    @Query("""
            select tp.tripPlan.id, count(tp)
            from TripPlace tp
            where tp.tripPlan.id in :tripPlanIds
            group by tp.tripPlan.id
            """)
    List<Object[]> findCountRowsByTripPlanIds(@Param("tripPlanIds") List<Integer> tripPlanIds);

    default Map<Integer, Long> countByTripPlanIds(List<Integer> tripPlanIds) {
        if (tripPlanIds == null || tripPlanIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = findCountRowsByTripPlanIds(tripPlanIds);
        Map<Integer, Long> placeCounts = new HashMap<>();
        for (Object[] row : rows) {
            placeCounts.put((Integer) row[0], (Long) row[1]);
        }
        return placeCounts;
    }

    @Modifying
    @Transactional
    @Query("""
            delete from TripPlace tp
            where tp.tripPlan.user.id = :userId
            """)
    int deleteByTripPlanUserId(@Param("userId") Integer userId);
}
