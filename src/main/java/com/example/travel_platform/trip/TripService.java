package com.example.travel_platform.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;
import com.example.travel_platform.weather.WeatherRegion;
import com.example.travel_platform.weather.WeatherRepository;
import com.example.travel_platform.weather.WeatherRepository.LandForecastRaw;
import com.example.travel_platform.weather.WeatherRepository.ShortTermForecastItem;
import com.example.travel_platform.weather.WeatherRepository.ShortTermForecastRaw;
import com.example.travel_platform.weather.WeatherRepository.TemperatureForecastRaw;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TripService {

    private static final int PLAN_PAGE_SIZE = 9;
    private static final String DEFAULT_CATEGORY = "result";

    @Value("${WEATHER_API_KEY:}")
    private String weatherApiKey;

    private final TripRepository tripRepository;
    private final TripPlanQueryRepository tripPlanQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final WeatherRepository weatherRepository;

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
        List<TripResponse.WeatherDTO> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();

        
        WeatherRegion region = WeatherRegion.fromInput(regionCode);
        if (region == null) region = WeatherRegion.SEOUL;

        String[] dayLabels = {"첫째 날", "둘째 날", "셋째 날", "넷째 날", "다섯째 날", "여섯째 날", "일곱째 날", "여덟째 날", "아홉째 날", "열째 날"};

        int dayIdx = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (dayIdx >= dayLabels.length) break;

            long diff = ChronoUnit.DAYS.between(today, date);
            TripResponse.WeatherDTO.WeatherDTOBuilder builder = TripResponse.WeatherDTO.builder()
                    .date(date.format(DateTimeFormatter.ofPattern("MM/dd")))
                    .dayLabel(dayLabels[dayIdx]);

            if (diff >= 0 && diff <= 10) {
                try {
                    fillRealWeatherData(builder, region, date, diff);
                    builder.hasData(true);
                } catch (Exception e) {
                    builder.icon("help_outline").temp("-").description("데이터 오류").hasData(false);
                }
            } else {
                builder.icon("help_outline").temp("-").description("날씨 정보 없음").hasData(false);
            }

            forecast.add(builder.build());
            dayIdx++;
        }

        return forecast;
    }

    private void fillRealWeatherData(TripResponse.WeatherDTO.WeatherDTOBuilder builder, 
                                    WeatherRegion region, 
                                    LocalDate date, long diff) {
        if (diff <= 3) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime baseDateTime = resolveShortTermBaseDateTime(now);
            
            ShortTermForecastRaw raw = 
                weatherRepository.fetchShortTermForecast(
                    region.getShortTermNx(), region.getShortTermNy(),
                    baseDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    baseDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HHmm"))
                );

            List<ShortTermForecastItem> items = raw.getItems().stream()
                .filter(item -> date.equals(item.getForecastDate()))
                .toList();

            if (items.isEmpty()) {
                builder.icon("help_outline").temp("-").description("정보 없음");
                return;
            }

            String sky = findWeatherValue(items, "SKY", "1400");
            String pty = findWeatherValue(items, "PTY", "1400");
            String tmp = findWeatherValue(items, "TMP", "1400");

            mapWeatherInfo(builder, sky, pty, tmp);
        } else {
            LocalDateTime announcementTime = resolveMidTermAnnouncementTime(LocalDateTime.now());
            String tmFc = announcementTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

            LandForecastRaw land = 
                weatherRepository.fetchLandForecast(region.getLandRegId(), tmFc);
            TemperatureForecastRaw temp = 
                weatherRepository.fetchTemperatureForecast(region.getTemperatureRegId(), tmFc);

            String wf = land.getText("wf" + diff);
            if (wf == null) wf = land.getText("wf" + diff + "Pm");
            
            Integer taMax = temp.getNumber("taMax" + diff);

            builder.description(wf != null ? wf : "맑음");
            builder.temp(taMax != null ? taMax + "도" : "-");
            
            if (wf != null && (wf.contains("비") || wf.contains("소나기"))) {
                builder.icon("rainy").colorClass("blue");
            } else if (wf != null && wf.contains("눈")) {
                builder.icon("cloudy_snowing").colorClass("white");
            } else if (wf != null && wf.contains("구름많음")) {
                builder.icon("partly_cloudy_day").colorClass("blue");
            } else if (wf != null && wf.contains("흐림")) {
                builder.icon("cloud").colorClass("gray");
            } else {
                builder.icon("sunny").colorClass("orange");
            }
        }
    }

    private void mapWeatherInfo(TripResponse.WeatherDTO.WeatherDTOBuilder builder, String sky, String pty, String tmp) {
        builder.temp((tmp != null ? tmp : "-") + "도");
        
        if (pty != null && !"0".equals(pty)) {
            switch (pty) {
                case "1", "4", "5" -> builder.icon("rainy").colorClass("blue").description("비");
                case "2", "6" -> builder.icon("cloudy_snowing").colorClass("white").description("비/눈");
                case "3", "7" -> builder.icon("cloudy_snowing").colorClass("white").description("눈");
                default -> builder.icon("rainy").colorClass("blue").description("비");
            }
        } else {
            switch (sky != null ? sky : "1") {
                case "1" -> builder.icon("sunny").colorClass("orange").description("맑음");
                case "3" -> builder.icon("partly_cloudy_day").colorClass("blue").description("구름많음");
                case "4" -> builder.icon("cloud").colorClass("gray").description("흐림");
                default -> builder.icon("sunny").colorClass("orange").description("맑음");
            }
        }
    }

    private String findWeatherValue(List<ShortTermForecastItem> items, String category, String preferredTime) {
        return items.stream()
            .filter(i -> category.equals(i.getCategory()))
            .sorted((a, b) -> {
                if (preferredTime.equals(a.getForecastTime())) return -1;
                if (preferredTime.equals(b.getForecastTime())) return 1;
                return a.getForecastTime().compareTo(b.getForecastTime());
            })
            .map(ShortTermForecastItem::getForecastValue)
            .findFirst()
            .orElse(null);
    }

    private LocalDateTime resolveShortTermBaseDateTime(LocalDateTime now) {
        LocalDateTime adjustedNow = now.minusMinutes(10);
        LocalDate date = adjustedNow.toLocalDate();
        LocalTime time = adjustedNow.toLocalTime();
        List<LocalTime> baseTimes = List.of(
            LocalTime.of(2, 0), LocalTime.of(5, 0), LocalTime.of(8, 0),
            LocalTime.of(11, 0), LocalTime.of(14, 0), LocalTime.of(17, 0),
            LocalTime.of(20, 0), LocalTime.of(23, 0));

        for (int i = baseTimes.size() - 1; i >= 0; i--) {
            if (!time.isBefore(baseTimes.get(i))) return LocalDateTime.of(date, baseTimes.get(i));
        }
        return LocalDateTime.of(date.minusDays(1), LocalTime.of(23, 0));
    }

    private LocalDateTime resolveMidTermAnnouncementTime(LocalDateTime now) {
        if (now.getHour() >= 18) return now.toLocalDate().atTime(18, 0);
        if (now.getHour() >= 6) return now.toLocalDate().atTime(6, 0);
        return now.toLocalDate().minusDays(1).atTime(18, 0);
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
                    p.getLatitude() == null ? null : BigDecimal.valueOf(p.getLatitude()),
                    p.getLongitude() == null ? null : BigDecimal.valueOf(p.getLongitude()),
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
        List<Integer> tripPlanIds = new ArrayList<>();
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

        List<TripPlace> sortedPlaces = new ArrayList<>(tripPlan.getPlaces());
        sortedPlaces.sort((left, right) -> compareTripPlaces(left, right));

        List<TripResponse.PlaceItemDTO> placeItems = new ArrayList<>();
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
