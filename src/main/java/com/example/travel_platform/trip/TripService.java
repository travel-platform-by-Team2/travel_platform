package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
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
        Integer actorUserId = requireSessionUserId(sessionUserId);
        validateCreatePlan(reqDTO);
        User user = findUser(actorUserId);
        TripPlan tripPlan = createTripPlan(user, reqDTO);
        return saveCreatedPlan(tripPlan);
    }

    @Transactional
    public TripResponse.PlaceAddedDTO addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        validateDayOrder(reqDTO.getDayOrder());
        TripPlan tripPlan = findOwnedPlan(sessionUserId, planId);
        TripPlace tripPlace = createTripPlace(tripPlan, reqDTO);
        return saveAddedPlace(planId, tripPlace);
    }

    public TripResponse.ListPageDTO getPlanList(Integer userId, String category, int page) {
        Integer actorUserId = requireSessionUserId(userId);
        PlanListQuery query = createPlanListQuery(actorUserId, category, page);
        PlanPageQueryResult planPageQueryResult = findPlanPage(query);
        List<TripResponse.SummaryDTO> plans = toPlanSummaryDTOs(planPageQueryResult.tripPlans(), query.today());

        return TripResponse.ListPageDTO.of(
                plans,
                query.currentPage(),
                planPageQueryResult.totalCount(),
                query.category(),
                PLAN_PAGE_SIZE);
    }

    public TripResponse.DetailPageDTO getPlanDetailPage(Integer sessionUserId, Integer planId) {
        return TripResponse.DetailPageDTO.of(getPlanDetail(sessionUserId, planId));
    }

    public TripResponse.PlacePageDTO getPlacePage(Integer sessionUserId, Integer planId, String kakaoMapAppKey) {
        TripResponse.DetailDTO detailDTO = getPlanDetail(sessionUserId, planId);
        return TripResponse.PlacePageDTO.of(detailDTO, kakaoMapAppKey);
    }

    public TripResponse.DetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = findOwnedPlanWithPlaces(sessionUserId, planId);
        List<TripResponse.PlaceItemDTO> places = toPlaceItems(tripPlan);
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

    private void validateCreatePlan(TripRequest.CreatePlanDTO reqDTO) {
        validatePlanDates(reqDTO.getStartDate(), reqDTO.getEndDate());
    }

    private Integer requireSessionUserId(Integer sessionUserId) {
        if (sessionUserId == null) {
            throw new Exception401("로그인이 필요합니다");
        }
        return sessionUserId;
    }

    private TripPlan createTripPlan(User user, TripRequest.CreatePlanDTO reqDTO) {
        return TripPlan.create(
                user,
                reqDTO.getTitle(),
                reqDTO.getRegion(),
                reqDTO.getWhoWith(),
                reqDTO.getStartDate(),
                reqDTO.getEndDate(),
                NOT_IMG);
    }

    private TripPlace createTripPlace(TripPlan tripPlan, TripRequest.AddPlaceDTO reqDTO) {
        return TripPlace.create(
                tripPlan,
                reqDTO.getPlaceName(),
                reqDTO.getAddress(),
                reqDTO.getLatitude(),
                reqDTO.getLongitude(),
                reqDTO.getDayOrder());
    }

    private TripResponse.CreatedDTO saveCreatedPlan(TripPlan tripPlan) {
        tripRepository.savePlan(tripPlan);
        return TripResponse.CreatedDTO.of(tripPlan);
    }

    private TripResponse.PlaceAddedDTO saveAddedPlace(Integer planId, TripPlace tripPlace) {
        tripPlaceRepository.save(tripPlace);
        long placeCount = tripPlaceRepository.countByTripPlanId(planId);
        return TripResponse.PlaceAddedDTO.of(tripPlace, placeCount);
    }

    private PlanListQuery createPlanListQuery(Integer userId, String category, int page) {
        return new PlanListQuery(userId, normalizeCategory(category), Math.max(page, 0), LocalDate.now());
    }

    private String normalizeCategory(String category) {
        if ("upcoming".equals(category) || "past".equals(category)) {
            return category;
        }
        return "result";
    }

    private PlanPageQueryResult findPlanPage(PlanListQuery query) {
        return findPlanPage(query.userId(), query.category(), query.today(), query.currentPage());
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

    private List<TripResponse.SummaryDTO> toPlanSummaryDTOs(List<TripPlan> tripPlans, LocalDate today) {
        return tripPlans.stream()
                .map(tripPlan -> toPlanSummaryDTO(tripPlan, today))
                .toList();
    }

    private List<TripResponse.PlaceItemDTO> toPlaceItems(TripPlan tripPlan) {
        if (tripPlan.getPlaces() == null) {
            return List.of();
        }

        return tripPlan.getPlaces().stream()
                .sorted((left, right) -> compareTripPlaces(left, right))
                .map(place -> TripResponse.PlaceItemDTO.from(place))
                .toList();
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
            throw new Exception400("?ы뻾 醫낅즺?쇱? ?쒖옉?쇰낫??鍮좊? ???놁뒿?덈떎.");
        }
    }

    private void validateDayOrder(Integer dayOrder) {
        if (dayOrder == null || dayOrder < 1) {
            throw new Exception400("?ы뻾 ?쇱감 ?뺣낫媛 ?щ컮瑜댁? ?딆뒿?덈떎.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("?ъ슜???뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));
    }

    private TripPlan findOwnedPlan(Integer sessionUserId, Integer planId) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        TripPlan tripPlan = tripRepository.findPlanById(planId)
                .orElseThrow(() -> new Exception404("?대떦 ?ы뻾 怨꾪쉷??李얠쓣 ???놁뒿?덈떎."));
        validateOwner(actorUserId, tripPlan);
        return tripPlan;
    }

    private TripPlan findOwnedPlanWithPlaces(Integer sessionUserId, Integer planId) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        TripPlan tripPlan = tripRepository.findPlanByIdWithPlaces(planId)
                .orElseThrow(() -> new Exception404("?대떦 ?ы뻾 怨꾪쉷??李얠쓣 ???놁뒿?덈떎."));
        validateOwner(actorUserId, tripPlan);
        return tripPlan;
    }

    private void validateOwner(Integer sessionUserId, TripPlan tripPlan) {
        if (!tripPlan.isOwnedBy(sessionUserId)) {
            throw new Exception403("沅뚰븳???놁뒿?덈떎.");
        }
    }

    private record PlanListQuery(Integer userId, String category, int currentPage, LocalDate today) {
    }

    private record PlanPageQueryResult(List<TripPlan> tripPlans, long totalCount) {
    }
}
