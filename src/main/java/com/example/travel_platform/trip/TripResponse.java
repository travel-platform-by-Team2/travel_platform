package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
                    .imgUrl(resolveImageUrl(tripPlan.getImgUrl()))
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

        public static PlaceItemDTO fromTripPlace(TripPlace tripPlace) {
            return PlaceItemDTO.builder()
                    .id(tripPlace.getId())
                    .placeName(tripPlace.getPlaceName())
                    .address(tripPlace.getAddress())
                    .latitude(tripPlace.getLatitude() == null ? null : tripPlace.getLatitude().doubleValue())
                    .longitude(tripPlace.getLongitude() == null ? null : tripPlace.getLongitude().doubleValue())
                    .dayOrder(tripPlace.getTripDay())
                    .imgUrl(resolveImageUrl(tripPlace.getImgUrl()))
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
        private long placeCount;
        private boolean hasPlaces;
        private List<DayGroupDTO> days;

        public static DetailDTO createPlanDetail(TripPlan tripPlan, List<PlaceItemDTO> places) {
            long nightCount = calculateNightCount(tripPlan.getStartDate(), tripPlan.getEndDate());
            long dayCount = nightCount + 1;
            String regionLabel = tripPlan.getRegionLabel();

            // 일차별 그룹화
            java.util.Map<Integer, List<PlaceItemDTO>> grouped = new java.util.TreeMap<>();
            for (PlaceItemDTO p : places) {
                if (p.getDayOrder() != null) {
                    grouped.computeIfAbsent(p.getDayOrder(), k -> new java.util.ArrayList<>()).add(p);
                }
            }

            List<DayGroupDTO> dayGroups = new java.util.ArrayList<>();
            for (int i = 1; i <= dayCount; i++) {
                LocalDate currentDate = tripPlan.getStartDate().plusDays(i - 1);
                dayGroups.add(DayGroupDTO.builder()
                        .tripDay(i)
                        .dateLabel(currentDate.toString()) // 추후 포맷팅 가능
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
                    .placeCount(places.size())
                    .hasPlaces(!places.isEmpty())
                    .days(dayGroups)
                    .build();
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

    private static String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "/images/dumimg.jpg";
        }
        return imageUrl;
    }

}
