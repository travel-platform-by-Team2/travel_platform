package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TripService {

    private static final int PLAN_PAGE_SIZE = 9;
    private static final String DEFAULT_CATEGORY = "result";

    @org.springframework.beans.factory.annotation.Value("${WEATHER_API_KEY:}")
    private String weatherApiKey;

    private final TripRepository tripRepository;
    private final TripPlanQueryRepository tripPlanQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final TripPlaceRepository tripPlaceRepository;

    private static final java.util.Map<String, WeatherConfig> REGION_CONFIGS = java.util.Map.ofEntries(
        java.util.Map.entry("seoul", new WeatherConfig(60, 127, "11B10101", "11B00000")),
        java.util.Map.entry("busan", new WeatherConfig(98, 76, "11H20201", "11H20000")),
        java.util.Map.entry("daegu", new WeatherConfig(89, 90, "11H10701", "11H10000")),
        java.util.Map.entry("incheon", new WeatherConfig(55, 124, "11B20201", "11B00000")),
        java.util.Map.entry("gwangju", new WeatherConfig(58, 74, "11F20501", "11F20000")),
        java.util.Map.entry("daejeon", new WeatherConfig(67, 100, "11C20401", "11C20000")),
        java.util.Map.entry("ulsan", new WeatherConfig(102, 84, "11H20101", "11H20000")),
        java.util.Map.entry("sejong", new WeatherConfig(66, 103, "11C20404", "11C20000")),
        java.util.Map.entry("gyeonggi", new WeatherConfig(60, 120, "11B20601", "11B00000")),
        java.util.Map.entry("gangwon", new WeatherConfig(73, 134, "11D10301", "11D10000")),
        java.util.Map.entry("chungbuk", new WeatherConfig(69, 107, "11C10301", "11C10000")),
        java.util.Map.entry("chungnam", new WeatherConfig(68, 100, "11C20101", "11C20000")),
        java.util.Map.entry("jeonbuk", new WeatherConfig(63, 89, "11F10201", "11F10000")),
        java.util.Map.entry("jeonnam", new WeatherConfig(51, 67, "11F20503", "11F20000")),
        java.util.Map.entry("gyeongbuk", new WeatherConfig(89, 91, "11H10701", "11H10000")),
        java.util.Map.entry("gyeongnam", new WeatherConfig(91, 77, "11H20301", "11H20000")),
        java.util.Map.entry("jeju", new WeatherConfig(52, 38, "11G00201", "11G00000"))
    );

    @Transactional
    public TripResponse.CreatedDTO createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        validatePlanDates(reqDTO.getStartDate(), reqDTO.getEndDate());

        User user = findUser(actorUserId);
        TripPlan tripPlan = createTripPlan(user, reqDTO);
        tripRepository.savePlan(tripPlan);

        return TripResponse.CreatedDTO.createCreatedPlan(tripPlan);
    }

    public TripResponse.ListPageDTO getPlanList(Integer sessionUserId, String category, int page) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        PlanListQuery query = createPlanListQuery(actorUserId, category, page);
        PlanPageQueryResult queryResult = findPlanPage(query);
        List<TripResponse.SummaryDTO> planModels = createPlanSummaries(queryResult.tripPlans(), query.today());

        return TripResponse.ListPageDTO.createListPage(
                planModels,
                query.currentPage(),
                queryResult.totalCount(),
                query.category(),
                PLAN_PAGE_SIZE);
    }

    public TripResponse.DetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = findOwnedPlanWithPlaces(sessionUserId, planId);
        List<TripResponse.PlaceItemDTO> placeModels = createPlaceItems(tripPlan);
        List<TripResponse.WeatherDTO> weatherForecast = getWeatherForecast(
                tripPlan.getRegion(),
                tripPlan.getStartDate(),
                tripPlan.getEndDate());
        return TripResponse.DetailDTO.createPlanDetail(tripPlan, placeModels, weatherForecast);
    }

    private List<TripResponse.WeatherDTO> getWeatherForecast(String regionCode, LocalDate startDate, LocalDate endDate) {
        List<TripResponse.WeatherDTO> forecast = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        WeatherConfig config = REGION_CONFIGS.getOrDefault(regionCode, REGION_CONFIGS.get("seoul"));

        String[] days = {"첫째 날", "둘째 날", "셋째 날", "넷째 날", "다섯째 날", "여섯째 날", "일곱째 날", "여덟째 날", "아홉째 날", "열째 날"};

        int dayIdx = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (dayIdx >= days.length) break;

            long diff = java.time.temporal.ChronoUnit.DAYS.between(today, date);
            TripResponse.WeatherDTO.WeatherDTOBuilder builder = TripResponse.WeatherDTO.builder()
                    .date(date.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd")))
                    .dayLabel(days[dayIdx]);

            if (diff >= 0 && diff <= 10) {
                fillWeatherData(builder, diff, config);
                builder.hasData(true);
            } else {
                builder.icon("help_outline")
                        .temp("-")
                        .description("날씨 정보 없음")
                        .hasData(false);
            }

            forecast.add(builder.build());
            dayIdx++;
        }

        return forecast;
    }

    private void fillWeatherData(TripResponse.WeatherDTO.WeatherDTOBuilder builder, long diff, WeatherConfig config) {
        int seed = (int) (diff + config.nx + config.ny);
        String[] icons = {"sunny", "partly_cloudy_day", "cloud", "rainy", "cloudy_snowing"};
        String[] descriptions = {"맑음", "구름조금", "흐림", "비", "눈"};
        String[] colors = {"orange", "blue", "gray", "blue", "white"};
        
        int idx = Math.abs(seed) % icons.length;
        int tempBase = 15 + (seed % 10);

        builder.icon(icons[idx])
               .colorClass(colors[idx])
               .temp(tempBase + "도")
               .description(descriptions[idx]);
    }

    private static class WeatherConfig {
        int nx;
        int ny;
        String regIdTemp;
        String regIdLand;

        WeatherConfig(int nx, int ny, String regIdTemp, String regIdLand) {
            this.nx = nx;
            this.ny = ny;
            this.regIdTemp = regIdTemp;
            this.regIdLand = regIdLand;
        }
    }

    public TripResponse.PlacePageDTO getPlacePage(Integer sessionUserId, Integer planId, String kakaoMapAppKey) {
        TripResponse.DetailDTO detailModel = getPlanDetail(sessionUserId, planId);
        return TripResponse.PlacePageDTO.createPlacePage(detailModel, kakaoMapAppKey);
    }

    @Transactional
    public TripResponse.PlaceAddedDTO addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        validateDayOrder(reqDTO.getDayOrder());

        TripPlan tripPlan = findOwnedPlan(sessionUserId, planId);
        TripPlace tripPlace = createTripPlace(tripPlan, reqDTO);
        tripPlaceRepository.save(tripPlace);

        long placeCount = tripPlaceRepository.countByTripPlanId(planId);
        return TripResponse.PlaceAddedDTO.createAddedPlace(tripPlace, placeCount);
    }

    private Integer requireSessionUserId(Integer sessionUserId) {
        if (sessionUserId == null) {
            throw new Exception401("로그인이 필요합니다");
        }
        return sessionUserId;
    }

    private User findUser(Integer userId) {
        return userQueryRepository.findUser(userId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
    }

    private TripPlan createTripPlan(User user, TripRequest.CreatePlanDTO reqDTO) {
        return TripPlan.create(
                user,
                reqDTO.getTitle(),
                resolveTripRegion(reqDTO.getRegion()),
                resolveCompanionType(reqDTO.getWhoWith()),
                reqDTO.getStartDate(),
                reqDTO.getEndDate(),
                null);
    }

    @Transactional
    public void addPlacesToPlan(Integer sessionUserId, Integer planId, TripRequest.AddPlacesDTO reqDTO) {
        TripPlan tripPlan = findOwnedPlan(sessionUserId, planId);
        if (reqDTO.getPlaces() == null) {
            return;
        }

        for (TripRequest.PlaceDTO p : reqDTO.getPlaces()) {
            TripPlace tripPlace = TripPlace.create(
                    tripPlan,
                    p.getPlaceName(),
                    p.getAddress(),
                    p.getLatitude() == null ? null : java.math.BigDecimal.valueOf(p.getLatitude()),
                    p.getLongitude() == null ? null : java.math.BigDecimal.valueOf(p.getLongitude()),
                    reqDTO.getTripDay(),
                    p.getImgUrl(),
                    p.getType());
            tripPlaceRepository.save(tripPlace);
        }
    }

    private TripPlace createTripPlace(TripPlan tripPlan, TripRequest.AddPlaceDTO reqDTO) {
        Integer tripDay = reqDTO.getDayOrder();
        return TripPlace.create(
                tripPlan,
                reqDTO.getPlaceName(),
                reqDTO.getAddress(),
                reqDTO.getLatitude(),
                reqDTO.getLongitude(),
                tripDay);
    }

    private PlanListQuery createPlanListQuery(Integer userId, String category, int page) {
        return new PlanListQuery(userId, normalizeCategory(category), Math.max(page, 0), LocalDate.now());
    }

    private String normalizeCategory(String category) {
        if ("upcoming".equals(category) || "past".equals(category)) {
            return category;
        }
        return DEFAULT_CATEGORY;
    }

    private PlanPageQueryResult findPlanPage(PlanListQuery query) {
        int offset = query.currentPage() * PLAN_PAGE_SIZE;

        return switch (query.category()) {
            case "upcoming" -> new PlanPageQueryResult(
                    tripPlanQueryRepository.findUpcomingPlanList(query.userId(), query.today(), offset, PLAN_PAGE_SIZE),
                    tripPlanQueryRepository.countUpcomingPlanList(query.userId(), query.today()));
            case "past" -> new PlanPageQueryResult(
                    tripPlanQueryRepository.findPastPlanList(query.userId(), query.today(), offset, PLAN_PAGE_SIZE),
                    tripPlanQueryRepository.countPastPlanList(query.userId(), query.today()));
            default -> new PlanPageQueryResult(
                    tripPlanQueryRepository.findPlanList(query.userId(), offset, PLAN_PAGE_SIZE),
                    tripPlanQueryRepository.countPlanList(query.userId()));
        };
    }

    private List<TripResponse.SummaryDTO> createPlanSummaries(List<TripPlan> tripPlans, LocalDate today) {
        Map<Integer, Long> placeCounts = tripPlaceRepository.countByTripPlanIds(createTripPlanIds(tripPlans));

        return tripPlans.stream()
                .map(tripPlan -> createPlanSummary(tripPlan, today, placeCounts))
                .toList();
    }

    private List<Integer> createTripPlanIds(List<TripPlan> tripPlans) {
        List<Integer> tripPlanIds = new java.util.ArrayList<>();
        for (TripPlan tripPlan : tripPlans) {
            tripPlanIds.add(tripPlan.getId());
        }
        return tripPlanIds;
    }

    private TripResponse.SummaryDTO createPlanSummary(
            TripPlan tripPlan,
            LocalDate today,
            Map<Integer, Long> placeCounts) {
        long placeCount = placeCounts.getOrDefault(tripPlan.getId(), 0L);
        return TripResponse.SummaryDTO.createPlanSummary(tripPlan, today, placeCount);
    }

    private List<TripResponse.PlaceItemDTO> createPlaceItems(TripPlan tripPlan) {
        if (tripPlan.getPlaces() == null) {
            return List.of();
        }

        List<TripPlace> sortedPlaces = new java.util.ArrayList<>(tripPlan.getPlaces());
        sortedPlaces.sort((left, right) -> compareTripPlaces(left, right));

        List<TripResponse.PlaceItemDTO> placeItems = new java.util.ArrayList<>();
        for (TripPlace sortedPlace : sortedPlaces) {
            placeItems.add(TripResponse.PlaceItemDTO.fromTripPlace(sortedPlace));
        }
        return placeItems;
    }

    private int compareTripPlaces(TripPlace left, TripPlace right) {
        int tripDayCompare = compareNullableInteger(left.getTripDay(), right.getTripDay());
        if (tripDayCompare != 0) {
            return tripDayCompare;
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

    private void validatePlanDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return;
        }
        if (endDate.isBefore(startDate)) {
            throw new Exception400("종료 날짜는 시작 날짜보다 빠를 수 없습니다.");
        }
    }

    private void validateDayOrder(Integer dayOrder) {
        if (dayOrder == null || dayOrder < 1) {
            throw new Exception400("여행 일차 정보가 올바르지 않습니다.");
        }
    }

    private TripRegion resolveTripRegion(String regionCode) {
        TripRegion tripRegion = TripRegion.fromCodeOrNull(regionCode);
        if (tripRegion == null) {
            throw new Exception400("유효한 여행 지역을 선택해주세요.");
        }
        return tripRegion;
    }

    private TripCompanionType resolveCompanionType(String whoWithCode) {
        TripCompanionType companionType = TripCompanionType.fromCodeOrNull(whoWithCode);
        if (companionType == null) {
            throw new Exception400("유효한 동행 유형을 선택해주세요.");
        }
        return companionType;
    }

    private TripPlan findOwnedPlan(Integer sessionUserId, Integer planId) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        TripPlan tripPlan = tripPlanQueryRepository.findPlan(planId)
                .orElseThrow(() -> new Exception404("여행 계획을 찾을 수 없습니다."));
        validateOwner(actorUserId, tripPlan);
        return tripPlan;
    }

    private TripPlan findOwnedPlanWithPlaces(Integer sessionUserId, Integer planId) {
        Integer actorUserId = requireSessionUserId(sessionUserId);
        TripPlan tripPlan = tripPlanQueryRepository.findPlanWithPlaces(planId)
                .orElseThrow(() -> new Exception404("여행 계획을 찾을 수 없습니다."));
        validateOwner(actorUserId, tripPlan);
        return tripPlan;
    }

    private void validateOwner(Integer sessionUserId, TripPlan tripPlan) {
        if (!tripPlan.isOwnedBy(sessionUserId)) {
            throw new Exception403("본인 여행 계획만 접근할 수 있습니다.");
        }
    }

    private record PlanListQuery(Integer userId, String category, int currentPage, LocalDate today) {
    }

    private record PlanPageQueryResult(List<TripPlan> tripPlans, long totalCount) {
    }
}
