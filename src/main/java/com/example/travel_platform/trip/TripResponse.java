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

        public static SummaryDTO of(TripPlan tripPlan, LocalDate today, long placeCount) {
            long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());
            boolean disabled = diff <= 0;

            return SummaryDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .imgUrl(resolveImageUrl(tripPlan.getImgUrl()))
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .regionLabel(toRegionLabel(tripPlan.getRegion()))
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
        private boolean isResult;
        private boolean isUpcoming;
        private boolean isPast;

        public static ListPageDTO of(List<SummaryDTO> plans,
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
                    .isResult("result".equals(category))
                    .isUpcoming("upcoming".equals(category))
                    .isPast("past".equals(category))
                    .build();
        }

        private static PaginationMeta createPagination(int currentPage, long totalCount, int size) {
            int totalPage = (int) Math.ceil((double) totalCount / size);
            int startPage = (currentPage / 10) * 10;
            int endPage = totalPage == 0 ? -1 : Math.min(startPage + 9, totalPage - 1);

            List<PageNumberDTO> pageNumbers = new ArrayList<>();
            for (int page = startPage; page <= endPage; page++) {
                pageNumbers.add(new PageNumberDTO(page, page + 1, page == currentPage));
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

        private record PaginationMeta(int totalPage,
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

        public static PlaceItemDTO from(TripPlace tripPlace) {
            return PlaceItemDTO.builder()
                    .id(tripPlace.getId())
                    .placeName(tripPlace.getPlaceName())
                    .address(tripPlace.getAddress())
                    .latitude(tripPlace.getLatitude() == null ? null : tripPlace.getLatitude().doubleValue())
                    .longitude(tripPlace.getLongitude() == null ? null : tripPlace.getLongitude().doubleValue())
                    .dayOrder(tripPlace.getDayOrder())
                    .build();
        }
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
        private List<PlaceItemDTO> places;

        public static DetailDTO of(TripPlan tripPlan, List<PlaceItemDTO> places) {
            long nightCount = calculateNightCount(tripPlan.getStartDate(), tripPlan.getEndDate());
            long dayCount = nightCount + 1;
            String regionLabel = toRegionLabel(tripPlan.getRegion());

            return DetailDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .formattedTitle(toFormattedTitle(tripPlan.getTitle(), regionLabel))
                    .region(tripPlan.getRegion())
                    .regionLabel(regionLabel)
                    .whoWith(tripPlan.getWhoWith())
                    .whoWithLabel(toWhoWithLabel(tripPlan.getWhoWith()))
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .travelPeriodLabel(formatTravelPeriod(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .nightCountLabel(nightCount + "박")
                    .dayCountLabel(dayCount + "일")
                    .placeCount(places.size())
                    .hasPlaces(!places.isEmpty())
                    .places(places)
                    .build();
        }
    }

    @Data
    @Builder
    public static class DetailPageDTO {
        private DetailDTO detail;

        public static DetailPageDTO of(DetailDTO detail) {
            return DetailPageDTO.builder()
                    .detail(detail)
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

        public static PlacePageDTO of(DetailDTO detail, String kakaoMapAppKey) {
            return PlacePageDTO.builder()
                    .detail(detail)
                    .kakaoMapAppKey(kakaoMapAppKey == null ? "" : kakaoMapAppKey)
                    .detailUrl("/trip/detail?id=" + detail.getId())
                    .saveUrl("/api/trips/" + detail.getId() + "/places")
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

        public static CreateFormDTO empty() {
            return CreateFormDTO.builder()
                    .title("")
                    .region("")
                    .whoWith("")
                    .build();
        }

        public static CreateFormDTO from(String title,
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
    }

    @Data
    @Builder
    public static class CreatedDTO {
        private Integer id;
        private String redirectUrl;

        public static CreatedDTO of(Integer id) {
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

        public static PlaceAddedDTO of(TripPlace tripPlace, long placeCount) {
            return PlaceAddedDTO.builder()
                    .id(tripPlace.getId())
                    .planId(tripPlace.getTripPlan().getId())
                    .placeName(tripPlace.getPlaceName())
                    .address(tripPlace.getAddress())
                    .dayOrder(tripPlace.getDayOrder())
                    .placeCount(placeCount)
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

    private static String toRegionLabel(String region) {
        if (region == null || region.isBlank()) {
            return "지역 정보 없음";
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

    private static String toWhoWithLabel(String whoWith) {
        if (whoWith == null || whoWith.isBlank()) {
            return "동행 정보 없음";
        }
        return whoWith;
    }
}
