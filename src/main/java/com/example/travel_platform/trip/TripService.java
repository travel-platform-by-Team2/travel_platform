package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public TripResponse.PlanListPageDTO getPlanList(Integer userId, String category, int page) {
        int size = 9; // 슬롯 갯수
        int offset = page * size;
        int blockSize = 10; // 1~10까지 페이징 사이즈
        LocalDate today = LocalDate.now();

        List<TripPlan> tripPlans;
        Long totalCount;

        if ("upcoming".equals(category)) {
            tripPlans = tripRepository.findUpcomingPlanListByUserId(userId, today, offset, size);
            totalCount = tripRepository.countUpcomingPlanByUserId(userId, today);
        } else if ("past".equals(category)) {
            tripPlans = tripRepository.findPastPlanListByUserId(userId, today, offset, size);
            totalCount = tripRepository.countPastPlanByUserId(userId, today);
        } else {
            tripPlans = tripRepository.findPlanListByUserId(userId, offset, size);
            totalCount = tripRepository.countPlanByUserId(userId);
        }

        List<TripResponse.PlanSummaryDTO> result = new ArrayList<>();

        for (TripPlan tripPlan : tripPlans) {
            String placeName = "장소 확인 안됨";

            if (tripPlan.getPlaces() != null && !tripPlan.getPlaces().isEmpty()) {
                placeName = tripPlan.getPlaces().get(0).getPlaceName();
            }

            long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());

            String dDay = "비활성화";
            boolean disabled = true;

            if (diff > 0) {
                dDay = "D-" + diff;
                disabled = false;
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

        int totalPage = (int) Math.ceil((double) totalCount / size);

        int startPage = (page / blockSize) * blockSize;
        int endPage = startPage + blockSize - 1;

        if (endPage >= totalPage) {
            endPage = totalPage - 1;
        }

        List<TripResponse.PageNumberDTO> pageNumbers = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(new TripResponse.PageNumberDTO(i, i + 1, i == page));
        }

        boolean hasPrev = startPage > 0;
        boolean hasNext = endPage < totalPage - 1;

        int prevPage = startPage - 1;
        int nextPage = endPage + 1;

        return TripResponse.PlanListPageDTO.builder()
                .plans(result)
                .currentPage(page)
                .displayPage(page + 1)
                .size(size)
                .totalCount(totalCount)
                .totalPage(totalPage)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .pageNumbers(pageNumbers)
                .startPage(startPage)
                .endPage(endPage)
                .category(category)
                .build();
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        // TODO: 단건 조회 + 소유권 검증
        // TODO: PlanDetailDTO 매핑
        return null;
    }
}
