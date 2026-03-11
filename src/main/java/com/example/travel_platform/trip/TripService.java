package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
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

    public List<TripResponse.PlanSummaryDTO> getPlanList(Integer userId, String category) {
        List<TripPlan> tripPlans = tripRepository.findPlanListByUserId(userId);
        LocalDate today = LocalDate.now();

        List<TripResponse.PlanSummaryDTO> result = new java.util.ArrayList<>();

        for (TripPlan tripPlan : tripPlans) {
            String placeName = "장소 확인 안됨";

            if (tripPlan.getPlaces() != null && !tripPlan.getPlaces().isEmpty()) {
                placeName = tripPlan.getPlaces().get(0).getPlaceName();
            }

            long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());

            String dDay;
            boolean disabled;

            if (diff > 0) { // d-day 계산
                dDay = "D-" + diff;
                disabled = false;
            } else {
                dDay = "비활성화";
                disabled = true;
            }

            TripResponse.PlanSummaryDTO dto = TripResponse.PlanSummaryDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .imgUrl(tripPlan.getImgUrl())
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .placeName(placeName)
                    .dDay(dDay)
                    .disabled(disabled)
                    .build();

            result.add(dto);
        }

        if ("upcoming".equals(category)) {
            return result.stream().filter(dto -> !dto.isDisabled()).toList();
        }
        if ("past".equals(category)) {
            return result.stream().filter(TripResponse.PlanSummaryDTO::isDisabled).toList();
        }
        return result;
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        // TODO: 단건 조회 + 소유권 검증
        // TODO: PlanDetailDTO 매핑
        return null;
    }
}
