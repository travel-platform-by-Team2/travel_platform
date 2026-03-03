package com.example.travel_platform.trip;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    @Transactional
    public void createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        // TODO: sessionUserId 소유권 검증
        // TODO: reqDTO 유효성 검증
        // TODO: TripPlan 엔티티 변환 후 저장
    }

    @Transactional
    public void addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        // TODO: planId 소유권 검증
        // TODO: reqDTO 유효성 검증
        // TODO: TripPlace 엔티티 변환 후 저장
    }

    public List<TripResponse.PlanSummaryDTO> getPlanList(Integer sessionUserId) {
        // TODO: 사용자별 여행 계획 목록 조회
        // TODO: PlanSummaryDTO 매핑
        return List.of();
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        // TODO: 단건 조회 + 소유권 검증
        // TODO: PlanDetailDTO 매핑
        return null;
    }
}

