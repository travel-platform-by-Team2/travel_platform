package com.example.travel_platform.trip;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {

    long countByTripPlanId(Integer tripPlanId);
}
