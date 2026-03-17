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
    private static final String NotImg = "/images/dumimg.jpg";

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
            String region = RegionLabel(tripPlan.getRegion());

            if (region == null || region.isBlank()) {
                region = "지역 정보 없음";
            }

            String imageUrl = tripPlan.getImgUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                imageUrl = NotImg;
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
                    .imgUrl(imageUrl)
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .placeName(region)
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

    // 지역 영어 db 한글로 출력
    private String RegionLabel(String region) {
        if (region == null || region.isBlank()) {
            return "지역 정보 없음";
        }

        return switch (region) {
            case "seoul" -> "서울특별시";
            case "busan" -> "부산광역시";
            case "daegu" -> "대구광역시";
            case "incheon" -> "인천광역시";
            case "gwangju" -> "광주광역시";
            case "daejeon" -> "대전광역시";
            case "ulsan" -> "울산광역시";
            case "sejong" -> "세종특별자치시";
            case "gyeonggi" -> "경기도";
            case "gangwon" -> "강원특별자치도";
            case "chungbuk" -> "충청북도";
            case "chungnam" -> "충청남도";
            case "jeonbuk" -> "전라북도";
            case "jeonnam" -> "전라남도";
            case "gyeongbuk" -> "경상북도";
            case "gyeongnam" -> "경상남도";
            case "jeju" -> "제주특별자치도";
            default -> region;
        };
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = tripRepository.findPlanById(planId)
                .orElseThrow(() -> new com.example.travel_platform._core.handler.ex.Exception404("해당 여행 계획을 찾을 수 없습니다."));

        if (!tripPlan.getUser().getId().equals(sessionUserId)) {
            throw new com.example.travel_platform._core.handler.ex.Exception403("권한이 없습니다.");
        }

        List<TripResponse.PlaceDTO> places = new ArrayList<>();
        if (tripPlan.getPlaces() != null) {
            places = tripPlan.getPlaces().stream().map(place -> 
                TripResponse.PlaceDTO.builder()
                    .id(place.getId())
                    .placeName(place.getPlaceName())
                    .address(place.getAddress())
                    .dayOrder(place.getDayOrder())
                    .build()
            ).toList();
        }

        return TripResponse.PlanDetailDTO.builder()
                .id(tripPlan.getId())
                .title(tripPlan.getTitle())
                .region(tripPlan.getRegion())
                .startDate(tripPlan.getStartDate())
                .endDate(tripPlan.getEndDate())
                .places(places)
                .build();
    }
}
