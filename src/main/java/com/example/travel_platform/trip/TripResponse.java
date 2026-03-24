package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TripResponse {

    @Data
    @Builder
    public static class SummaryDTO {
        private Integer id;
        private String title;
        private String imgUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private String dateRangeLabel;
        private String regionLabel;
        private String dDay;
        private boolean disabled;
        private long placeCount;

        public static SummaryDTO createPlanSummary(TripPlan tripPlan, LocalDate today, long placeCount) {
            long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());
            boolean disabled = diff <= 0;

            return SummaryDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .imgUrl(resolveImageUrl(tripPlan.getImgUrl(), null, null))
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .regionLabel(tripPlan.getRegionLabel())
                    .dDay(disabled ? "비활성화" : "D-" + diff)
                    .disabled(disabled)
                    .placeCount(placeCount)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ListPageDTO {
        private List<SummaryDTO> plans;
        private int currentPage;
        private int displayPage;
        private int size;
        private long totalCount;
        private int totalPage;
        private boolean hasPrev;
        private boolean hasNext;
        private int prevPage;
        private int nextPage;
        private int startPage;
        private int endPage;
        private List<PageNumberDTO> pageNumbers;
        private String category;
        private boolean result;
        private boolean upcoming;
        private boolean past;

        public static ListPageDTO createListPage(
                List<SummaryDTO> plans,
                int currentPage,
                long totalCount,
                String category,
                int size) {
            PaginationMeta pagination = createPagination(currentPage, totalCount, size);

            return ListPageDTO.builder()
                    .plans(plans)
                    .currentPage(currentPage)
                    .displayPage(currentPage + 1)
                    .size(size)
                    .totalCount(totalCount)
                    .totalPage(pagination.totalPage())
                    .hasPrev(pagination.hasPrev())
                    .hasNext(pagination.hasNext())
                    .prevPage(pagination.prevPage())
                    .nextPage(pagination.nextPage())
                    .startPage(pagination.startPage())
                    .endPage(pagination.endPage())
                    .pageNumbers(pagination.pageNumbers())
                    .category(category)
                    .result("result".equals(category))
                    .upcoming("upcoming".equals(category))
                    .past("past".equals(category))
                    .build();
        }

        private static PaginationMeta createPagination(int currentPage, long totalCount, int size) {
            int totalPage = (int) Math.ceil((double) totalCount / size);
            int startPage = (currentPage / 10) * 10;
            int endPage = totalPage == 0 ? -1 : Math.min(startPage + 9, totalPage - 1);

            List<PageNumberDTO> pageNumbers = new ArrayList<>();
            for (int page = startPage; page <= endPage; page++) {
                pageNumbers.add(PageNumberDTO.createPageNumber(page, page + 1, page == currentPage));
            }

            return new PaginationMeta(
                    totalPage,
                    startPage,
                    endPage,
                    startPage > 0,
                    endPage < totalPage - 1,
                    startPage - 1,
                    endPage + 1,
                    pageNumbers);
        }

        private record PaginationMeta(
                int totalPage,
                int startPage,
                int endPage,
                boolean hasPrev,
                boolean hasNext,
                int prevPage,
                int nextPage,
                List<PageNumberDTO> pageNumbers) {
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageNumberDTO {
        private int page;
        private int displayPage;
        private boolean current;

        public static PageNumberDTO createPageNumber(int page, int displayPage, boolean current) {
            return new PageNumberDTO(page, displayPage, current);
        }
    }

    @Data
    @Builder
    public static class PlaceItemDTO {
        private Integer id;
        private String placeName;
        private String address;
        private Double latitude;
        private Double longitude;
        private Integer dayOrder;
        private String imgUrl;
        private boolean isHotel;

        public static PlaceItemDTO fromTripPlace(TripPlace tripPlace) {
            return PlaceItemDTO.builder()
                    .id(tripPlace.getId())
                    .placeName(tripPlace.getPlaceName())
                    .address(tripPlace.getAddress())
                    .latitude(tripPlace.getLatitude() == null ? null : tripPlace.getLatitude().doubleValue())
                    .longitude(tripPlace.getLongitude() == null ? null : tripPlace.getLongitude().doubleValue())
                    .dayOrder(tripPlace.getTripDay())
                    .imgUrl(resolveImageUrl(tripPlace.getImgUrl(), tripPlace.getPlaceType(), tripPlace.getPlaceName()))
                    .isHotel("hotel".equals(tripPlace.getPlaceType()))
                    .build();
        }
    }

    @Data
    @Builder
    public static class DayGroupDTO {
        private Integer tripDay;
        private String dateLabel;
        private List<PlaceItemDTO> items;
    }

    @Data
    @Builder
    public static class WeatherDTO {
        private String date; // "10/15"
        private String dayLabel; // "첫째 날"
        private String icon; // "sunny", "cloudy", "rainy", "cloudy_snowing"
        private String colorClass; // "orange", "blue", "gray" 등
        private String temp; // "22도"
        private String description; // "맑음"
        private boolean hasData; // 데이터 존재 여부
    }

    @Data
    @Builder
    public static class RecommendationDTO {
        private String icon; // "apparel", "umbrella", "check_circle" 등
        private String title; // "가벼운 아우터"
        private String description; // "얇은 셔츠나 니트"
        private boolean isLast; // UI 구조 유지를 위한 플래그
    }

    @Data
    @Builder
    public static class DetailDTO {
        private Integer id;
        private String title;
        private String formattedTitle;
        private String region;
        private String regionLabel;
        private String whoWith;
        private String whoWithLabel;
        private LocalDate startDate;
        private LocalDate endDate;
        private String dateRangeLabel;
        private String travelPeriodLabel;
        private String nightCountLabel;
        private String dayCountLabel;
        private String dDayLabel;
        private String avgTempLabel; // "평균 20도 기준"
        private List<RecommendationDTO> recommendations;
        private long placeCount;
        private boolean hasPlaces;
        private List<DayGroupDTO> days;
        private List<WeatherDTO> weatherForecast;

        public static DetailDTO createPlanDetail(TripPlan tripPlan, List<PlaceItemDTO> places,
                List<WeatherDTO> weatherForecast) {
            LocalDate today = LocalDate.now();
            long dDay = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());
            String dDayLabel = dDay == 0 ? "D-Day" : (dDay > 0 ? "D-" + dDay : "D+" + Math.abs(dDay));

            long nightCount = calculateNightCount(tripPlan.getStartDate(), tripPlan.getEndDate());
            long dayCount = nightCount + 1;
            String regionLabel = tripPlan.getRegionLabel();

            // 평균 기온 및 준비물 계산
            int totalTemp = 0;
            int tempCount = 0;
            boolean hasRain = false;
            for (WeatherDTO w : weatherForecast) {
                if (w.isHasData()) {
                    try {
                        String tempStr = w.getTemp().replace("도", "");
                        totalTemp += Integer.parseInt(tempStr);
                        tempCount++;
                        if ("rainy".equals(w.getIcon()))
                            hasRain = true;
                    } catch (Exception ignored) {
                    }
                }
            }

            int avgTemp = tempCount > 0 ? totalTemp / tempCount : 20; // 데이터 없으면 기본 20도
            List<RecommendationDTO> recommendations = generateRecommendations(avgTemp, hasRain);

            // 일차별 그룹화
            Map<Integer, List<PlaceItemDTO>> grouped = new TreeMap<>();
            for (PlaceItemDTO p : places) {
                if (p.getDayOrder() != null) {
                    grouped.computeIfAbsent(p.getDayOrder(), k -> new ArrayList<>()).add(p);
                }
            }

            List<DayGroupDTO> dayGroups = new ArrayList<>();
            for (int i = 1; i <= dayCount; i++) {
                LocalDate currentDate = tripPlan.getStartDate().plusDays(i - 1);
                dayGroups.add(DayGroupDTO.builder()
                        .tripDay(i)
                        .dateLabel(currentDate.toString())
                        .items(grouped.getOrDefault(i, List.of()))
                        .build());
            }

            return DetailDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .formattedTitle(toFormattedTitle(tripPlan.getTitle(), regionLabel))
                    .region(tripPlan.getRegion())
                    .regionLabel(regionLabel)
                    .whoWith(tripPlan.getWhoWith())
                    .whoWithLabel(tripPlan.getWhoWithLabel())
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .travelPeriodLabel(formatTravelPeriod(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .nightCountLabel(nightCount + "박")
                    .dayCountLabel(dayCount + "일")
                    .dDayLabel(dDayLabel)
                    .avgTempLabel("평균 " + avgTemp + "도 기준")
                    .recommendations(recommendations)
                    .placeCount(places.size())
                    .hasPlaces(!places.isEmpty())
                    .days(dayGroups)
                    .weatherForecast(weatherForecast)
                    .build();
        }

        private static List<RecommendationDTO> generateRecommendations(int avgTemp, boolean hasRain) {
            List<RecommendationDTO> list = new ArrayList<>();

            // 1. 온도별 의류 추천
            if (avgTemp <= 5) {
                list.add(
                        RecommendationDTO.builder().icon("apparel").title("두꺼운 외투").description("패딩이나 두꺼운 코트").build());
            } else if (avgTemp <= 10) {
                list.add(RecommendationDTO.builder().icon("apparel").title("코트와 니트").description("가을/겨울용 외투와 니트")
                        .build());
            } else if (avgTemp <= 17) {
                list.add(RecommendationDTO.builder().icon("apparel").title("가벼운 아우터").description("자켓이나 트렌치 코트")
                        .build());
            } else if (avgTemp <= 22) {
                list.add(RecommendationDTO.builder().icon("apparel").title("긴팔 상의").description("셔츠나 가벼운 가디건").build());
            } else if (avgTemp <= 27) {
                list.add(RecommendationDTO.builder().icon("apparel").title("반팔과 얇은 셔츠").description("통기성 좋은 여름 옷")
                        .build());
            } else {
                list.add(RecommendationDTO.builder().icon("apparel").title("시원한 민소매").description("얇은 소재의 여름 옷")
                        .build());
            }

            // 2. 날씨별 추천 (비 소식 등)
            if (hasRain) {
                list.add(RecommendationDTO.builder().icon("umbrella").title("우산").description("비 소식 대비").build());
            } else {
                list.add(RecommendationDTO.builder().icon("wb_sunny").title("자외선 차단제").description("맑은 날씨 대비").build());
            }

            // 3. 고정 필수템
            list.add(RecommendationDTO.builder().icon("check_circle").title("챙김 목록").description("보조배터리, 편한 신발")
                    .isLast(true).build());

            return list;
        }
    }

    @Data
    @Builder
    public static class PlacePageDTO {
        private DetailDTO detail;
        private String kakaoMapAppKey;
        private String detailUrl;
        private String saveUrl;
        private long existingCount;

        public static PlacePageDTO createPlacePage(DetailDTO detail, String kakaoMapAppKey) {
            return PlacePageDTO.builder()
                    .detail(detail)
                    .kakaoMapAppKey(kakaoMapAppKey == null ? "" : kakaoMapAppKey)
                    .detailUrl("/trip/detail?id=" + detail.getId())
                    .saveUrl("/api/trips/" + detail.getId() + "/places/bulk")
                    .existingCount(detail.getPlaceCount())
                    .build();
        }
    }

    @Data
    @Builder
    public static class CreateFormDTO {
        private String title;
        private String region;
        private String whoWith;
        private LocalDate startDate;
        private LocalDate endDate;
        private String titleError;
        private String regionError;
        private String whoWithError;
        private String startDateError;
        private String endDateError;

        public static CreateFormDTO createEmptyForm() {
            return CreateFormDTO.builder()
                    .title("")
                    .region("")
                    .whoWith("")
                    .build();
        }

        public static CreateFormDTO createCreateForm(
                String title,
                String region,
                String whoWith,
                LocalDate startDate,
                LocalDate endDate,
                String titleError,
                String regionError,
                String whoWithError,
                String startDateError,
                String endDateError) {
            return CreateFormDTO.builder()
                    .title(blank(title))
                    .region(blank(region))
                    .whoWith(blank(whoWith))
                    .startDate(startDate)
                    .endDate(endDate)
                    .titleError(titleError)
                    .regionError(regionError)
                    .whoWithError(whoWithError)
                    .startDateError(startDateError)
                    .endDateError(endDateError)
                    .build();
        }

        public String getStartDateValue() {
            if (startDate == null) {
                return "";
            }
            return startDate.toString();
        }

        public String getEndDateValue() {
            if (endDate == null) {
                return "";
            }
            return endDate.toString();
        }
    }

    @Data
    @Builder
    public static class CreatedDTO {
        private Integer id;
        private String redirectUrl;

        public static CreatedDTO createCreatedPlan(TripPlan tripPlan) {
            return createCreatedPlan(tripPlan.getId());
        }

        public static CreatedDTO createCreatedPlan(Integer id) {
            return CreatedDTO.builder()
                    .id(id)
                    .redirectUrl("/trip/detail?id=" + id)
                    .build();
        }
    }

    @Data
    @Builder
    public static class PlaceAddedDTO {
        private Integer id;
        private Integer planId;
        private String placeName;
        private String address;
        private Integer dayOrder;
        private long placeCount;
        private String detailUrl;

        public static PlaceAddedDTO createAddedPlace(TripPlace tripPlace, long placeCount) {
            Integer planId = tripPlace.getTripPlan().getId();
            return PlaceAddedDTO.builder()
                    .id(tripPlace.getId())
                    .planId(planId)
                    .placeName(tripPlace.getPlaceName())
                    .address(tripPlace.getAddress())
                    .dayOrder(tripPlace.getTripDay())
                    .placeCount(placeCount)
                    .detailUrl("/trip/detail?id=" + planId)
                    .build();
        }
    }

    private static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate + " - " + endDate;
    }

    private static String formatTravelPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }

        long nightCount = calculateNightCount(startDate, endDate);
        long dayCount = nightCount + 1;
        return formatDateRange(startDate, endDate) + " (" + nightCount + "박 " + dayCount + "일)";
    }

    private static long calculateNightCount(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(startDate, endDate), 0);
    }

    private static String toFormattedTitle(String title, String regionLabel) {
        if (regionLabel != null && !regionLabel.isBlank() && !"지역 정보 없음".equals(regionLabel)) {
            return regionLabel + " 여행";
        }
        if (title == null || title.isBlank()) {
            return "여행";
        }
        return title;
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }

    private static String resolveImageUrl(String imageUrl, String type, String placeName) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.startsWith("data:")) {
            // 1. 타입을 우선 확인 (상세 페이지의 장소들)
            if ("hotel".equals(type)) {
                return "/images/hotel.png";
            }
            if ("attraction".equals(type)) {
                return "/images/place.png";
            }

            // 2. 타입이 없는 경우(기존 데이터) 이름을 기반으로 판단
            if (placeName != null) {
                String name = placeName.toLowerCase();
                if (name.contains("호텔") || name.contains("펜션") || name.contains("민박") ||
                        name.contains("리조트") || name.contains("게스트하우스") || name.contains("숙소")) {
                    return "/images/hotel.png";
                }
            }

            // 3. 기본값은 기존 더미 이미지 (trip-list 등 유형이 없는 경우)
            return "/images/dumimg.jpg";
        }
        return imageUrl;
    }
}
