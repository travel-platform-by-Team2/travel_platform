package com.example.travel_platform.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {
    // 공통 기능(save, findById 등)은 이미 들어있어서
    // 특별한 조회 기능이 없다면 비워두셔도 됩니다!
}
