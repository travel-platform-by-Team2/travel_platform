package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripPlaceRepository tripPlaceRepository; // 추가!

    // TripService.java
    @Transactional
    public void createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        // 1. 세션 유저 정보 조회 (유저가 존재하는지 확인)
        // userRepository가 주입되어 있어야 합니다.
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 2. DTO 데이터를 엔티티(TripPlan)로 변환
        TripPlan tripPlan = new TripPlan();
        tripPlan.setUser(user); // 작성자 설정
        tripPlan.setTitle(reqDTO.getTitle()); // 여행 제목
        tripPlan.setWhoWith(reqDTO.getWhoWith()); // 누구와 함께 (추가된 필드)
        tripPlan.setStartDate(reqDTO.getStartDate()); // 시작일
        tripPlan.setEndDate(reqDTO.getEndDate()); // 종료일

        // 기본 이미지 설정 (엔티티에 nullable=false 설정이 되어 있으므로 필수)
        tripPlan.setImgUrl("placeholder-card.svg");

        // 3. DB에 저장
        tripRepository.savePlan(tripPlan);
    }

    @Transactional
    public void addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        // 1. 여행 계획 조회
        TripPlan tripPlan = tripRepository.findPlanById(planId).orElseThrow();

        // 2. 장소 엔티티 생성
        TripPlace tripPlace = new TripPlace();
        tripPlace.setTripPlan(tripPlan); // 어느 계획의 장소인지 연결
        tripPlace.setPlaceName(reqDTO.getPlaceName());
        tripPlace.setAddress(reqDTO.getAddress());
        // ... 나머지 세팅 ...
        // 3. JpaRepository의 save()로 저장!
        tripPlaceRepository.save(tripPlace);
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

            if (tripPlan.getRegion() != null && !tripPlan.getRegion().isBlank()) {
                placeName = tripPlan.getRegion();
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
