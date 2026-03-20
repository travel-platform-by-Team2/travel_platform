package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
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

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripPlaceRepository tripPlaceRepository;

    @Transactional
    public TripResponse.CreatedDTO createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        validatePlanDates(reqDTO.getStartDate(), reqDTO.getEndDate());
        User user = findUser(sessionUserId);

        TripPlan tripPlan = TripPlan.create(
                user,
                reqDTO.getTitle(),
                reqDTO.getRegion(),
                reqDTO.getWhoWith(),
                reqDTO.getStartDate(),
                reqDTO.getEndDate(),
                NOT_IMG);

        tripRepository.savePlan(tripPlan);
        return TripResponse.CreatedDTO.of(tripPlan.getId());
    }

    @Transactional
    public TripResponse.PlaceAddedDTO addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        validateDayOrder(reqDTO.getDayOrder());
        TripPlan tripPlan = findOwnedPlan(sessionUserId, planId);

        TripPlace tripPlace = TripPlace.create(
                tripPlan,
                reqDTO.getPlaceName(),
                reqDTO.getAddress(),
                reqDTO.getLatitude(),
                reqDTO.getLongitude(),
                reqDTO.getDayOrder());

        tripPlaceRepository.save(tripPlace);
        long placeCount = tripPlaceRepository.countByTripPlanId(planId);
        return TripResponse.PlaceAddedDTO.of(tripPlace, placeCount);
    }

    public TripResponse.ListPageDTO getPlanList(Integer userId, String category, int page) {
        int currentPage = Math.max(page, 0);
        LocalDate today = LocalDate.now();
        String normalizedCategory = normalizeCategory(category);

        PlanPageQueryResult planPageQueryResult = findPlanPage(userId, normalizedCategory, today, currentPage);
        List<TripResponse.SummaryDTO> plans = planPageQueryResult.tripPlans().stream()
                .map(tripPlan -> toPlanSummaryDTO(tripPlan, today))
                .toList();

        return TripResponse.ListPageDTO.of(
                plans,
                currentPage,
                planPageQueryResult.totalCount(),
                normalizedCategory,
                PLAN_PAGE_SIZE);
    }

    public TripResponse.DetailPageDTO getPlanDetailPage(Integer sessionUserId, Integer planId) {
        return TripResponse.DetailPageDTO.of(getPlanDetail(sessionUserId, planId));
    }

    public TripResponse.PlacePageDTO getPlacePage(Integer sessionUserId, Integer planId, String kakaoMapAppKey) {
        return TripResponse.PlacePageDTO.of(getPlanDetail(sessionUserId, planId), kakaoMapAppKey);
    }

    public TripResponse.DetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = findOwnedPlanWithPlaces(sessionUserId, planId);

        List<TripResponse.PlaceItemDTO> places = tripPlan.getPlaces() == null
                ? List.of()
                : tripPlan.getPlaces().stream()
                        .sorted((left, right) -> compareTripPlaces(left, right))
                        .map(place -> TripResponse.PlaceItemDTO.from(place))
                        .toList();

        return TripResponse.DetailDTO.of(tripPlan, places);
    }

    private int compareTripPlaces(TripPlace left, TripPlace right) {
        int dayOrderCompare = compareNullableInteger(left.getDayOrder(), right.getDayOrder());
        if (dayOrderCompare != 0) {
            return dayOrderCompare;
        }
        return compareNullableInteger(left.getId(), right.getId());
    }

    private int compareNullableInteger(Integer left, Integer right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return Integer.compare(left, right);
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

    private TripResponse.SummaryDTO toPlanSummaryDTO(TripPlan tripPlan, LocalDate today) {
        long placeCount = tripPlaceRepository.countByTripPlanId(tripPlan.getId());
        return TripResponse.SummaryDTO.of(tripPlan, today, placeCount);
    }

    private void validatePlanDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return;
        }
        if (endDate.isBefore(startDate)) {
            throw new Exception400("여행 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validateDayOrder(Integer dayOrder) {
        if (dayOrder == null || dayOrder < 1) {
            throw new Exception400("여행 일차 정보가 올바르지 않습니다.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
    }

    private TripPlan findOwnedPlan(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = tripRepository.findPlanById(planId)
                .orElseThrow(() -> new Exception404("해당 여행 계획을 찾을 수 없습니다."));
        validateOwner(sessionUserId, tripPlan);
        return tripPlan;
    }

    private TripPlan findOwnedPlanWithPlaces(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = tripRepository.findPlanByIdWithPlaces(planId)
                .orElseThrow(() -> new Exception404("해당 여행 계획을 찾을 수 없습니다."));
        validateOwner(sessionUserId, tripPlan);
        return tripPlan;
    }

    private void validateOwner(Integer sessionUserId, TripPlan tripPlan) {
        if (!tripPlan.isOwnedBy(sessionUserId)) {
            throw new Exception403("권한이 없습니다.");
        }
    }

    private record PlanPageQueryResult(List<TripPlan> tripPlans, long totalCount) {
    }
}
