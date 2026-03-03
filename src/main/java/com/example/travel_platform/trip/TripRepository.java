package com.example.travel_platform.trip;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TripRepository {

    private final EntityManager em;

    public TripPlan savePlan(TripPlan tripPlan) {
        // TODO: 저장 전략 확정 후 persist/merge 처리
        return tripPlan;
    }

    public TripPlace savePlace(TripPlace tripPlace) {
        // TODO: 장소 저장 처리
        return tripPlace;
    }

    public Optional<TripPlan> findPlanById(Integer planId) {
        // TODO: planId 기준 조회 쿼리 구현
        return Optional.empty();
    }

    public List<TripPlan> findPlanListByUserId(Integer userId) {
        // TODO: 사용자별 여행 계획 목록 조회 쿼리 구현
        return List.of();
    }
}

