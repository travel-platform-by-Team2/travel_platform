package com.example.travel_platform.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {

    long countByTripPlanId(Integer tripPlanId);

    @Modifying
    @Transactional
    @Query("""
            delete from TripPlace tp
            where tp.tripPlan.user.id = :userId
            """)
    int deleteByTripPlanUserId(@Param("userId") Integer userId);
}
