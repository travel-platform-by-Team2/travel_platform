package com.example.travel_platform.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Modifying
    @Transactional
    @Query("""
            delete from Booking b
            where b.user.id = :userId
            """)
    int deleteByUserId(@Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("""
            delete from Booking b
            where b.tripPlan.user.id = :userId
            """)
    int deleteByTripPlanUserId(@Param("userId") Integer userId);
}
