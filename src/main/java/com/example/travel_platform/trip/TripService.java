package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TripService {

    private static final int PLAN_PAGE_SIZE = 9;
    private static final String NOT_IMG = "/images/dumimg.jpg";
    // private static final String DEFAULT_PLAN_IMAGE =
    // "/images/placeholder-card.svg";
    private static final String EMPTY_REGION_LABEL = "지역 정보 없음";
    private static final String DISABLED_D_DAY = "비활성화";

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripPlaceRepository tripPlaceRepository;

    @Transactional
    public void createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TripPlan tripPlan = TripPlan.create(
                user,
                reqDTO.getTitle(),
                reqDTO.getRegion(),
                reqDTO.getWhoWith(),
                reqDTO.getStartDate(),
                reqDTO.getEndDate(),
                NOT_IMG);

        tripRepository.savePlan(tripPlan);
    }

    @Transactional
    public void addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        TripPlan tripPlan = tripRepository.findPlanById(planId).orElseThrow();

        TripPlace tripPlace = TripPlace.create(
                tripPlan,
                reqDTO.getPlaceName(),
                reqDTO.getAddress(),
                reqDTO.getLatitude(),
                reqDTO.getLongitude(),
                reqDTO.getDayOrder());

        tripPlaceRepository.save(tripPlace);
    }

    public TripResponse.PlanListPageDTO getPlanList(Integer userId, String category, int page) {
        int currentPage = Math.max(page, 0);
        LocalDate today = LocalDate.now();
        String normalizedCategory = normalizeCategory(category);

        PlanPageQueryResult planPageQueryResult = findPlanPage(userId, normalizedCategory, today, currentPage);
        List<TripResponse.PlanSummaryDTO> plans = planPageQueryResult.tripPlans().stream()
                .map(tripPlan -> toPlanSummaryDTO(tripPlan, today))
                .toList();

        return TripResponse.PlanListPageDTO.of(
                plans,
                currentPage,
                planPageQueryResult.totalCount(),
                normalizedCategory,
                PLAN_PAGE_SIZE);
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = tripRepository.findPlanById(planId)
                .orElseThrow(() -> new Exception404("해당 여행 계획을 찾을 수 없습니다."));

        if (!tripPlan.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("권한이 없습니다.");
        }

        List<TripResponse.PlaceDTO> places = tripPlan.getPlaces() == null
                ? List.of()
                : tripPlan.getPlaces().stream().map(TripResponse.PlaceDTO::new).toList();

        return new TripResponse.PlanDetailDTO(tripPlan, regionLabel(tripPlan.getRegion()), places);
    }

    private String normalizeCategory(String category) {
        if ("upcoming".equals(category) || "past".equals(category)) {
            return category;
        }
        return "result";
    }

    private PlanPageQueryResult findPlanPage(Integer userId, String normalizedCategory, LocalDate today, int page) {
        int offset = page * PLAN_PAGE_SIZE;

        return switch (normalizedCategory) {
            case "upcoming" -> new PlanPageQueryResult(
                    tripRepository.findUpcomingPlanListByUserId(userId, today, offset, PLAN_PAGE_SIZE),
                    tripRepository.countUpcomingPlanByUserId(userId, today));
            case "past" -> new PlanPageQueryResult(
                    tripRepository.findPastPlanListByUserId(userId, today, offset, PLAN_PAGE_SIZE),
                    tripRepository.countPastPlanByUserId(userId, today));
            default -> new PlanPageQueryResult(
                    tripRepository.findPlanListByUserId(userId, offset, PLAN_PAGE_SIZE),
                    tripRepository.countPlanByUserId(userId));
        };
    }

    private TripResponse.PlanSummaryDTO toPlanSummaryDTO(TripPlan tripPlan, LocalDate today) {
        long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());
        boolean disabled = diff <= 0;
        String dDay = disabled ? DISABLED_D_DAY : "D-" + diff;

        return new TripResponse.PlanSummaryDTO(
                tripPlan,
                resolveImageUrl(tripPlan.getImgUrl()),
                regionLabel(tripPlan.getRegion()),
                dDay,
                disabled);
    }

    private String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return NOT_IMG;
        }
        return imageUrl;
    }

    private String regionLabel(String region) {
        if (region == null || region.isBlank()) {
            return EMPTY_REGION_LABEL;
        }

        return switch (region) {
            case "seoul" -> "서울";
            case "busan" -> "부산";
            case "daegu" -> "대구";
            case "incheon" -> "인천";
            case "gwangju" -> "광주";
            case "daejeon" -> "대전";
            case "ulsan" -> "울산";
            case "sejong" -> "세종";
            case "gyeonggi" -> "경기도";
            case "gangwon" -> "강원도";
            case "chungbuk" -> "충청북도";
            case "chungnam" -> "충청남도";
            case "jeonbuk" -> "전라북도";
            case "jeonnam" -> "전라남도";
            case "gyeongbuk" -> "경상북도";
            case "gyeongnam" -> "경상남도";
            case "jeju" -> "제주도";
            default -> region;
        };
    }

    private record PlanPageQueryResult(List<TripPlan> tripPlans, long totalCount) {
    }
}
