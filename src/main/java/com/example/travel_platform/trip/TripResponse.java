package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

// 여행 계획/장소 조회 응답 모델을 정의하는 DTO 모음
public class TripResponse {

    @Data
    @AllArgsConstructor
    public static class PlanListPageDTO {
        private static final int PAGE_BLOCK_SIZE = 10;

        private List<PlanSummaryDTO> plans;
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

        public static PlanListPageDTO of(List<PlanSummaryDTO> plans,
                int currentPage,
                long totalCount,
                String category,
                int size) {
            PaginationMeta pagination = createPagination(currentPage, totalCount, size);

            return new PlanListPageDTO(
                    plans,
                    currentPage,
                    currentPage + 1,
                    size,
                    totalCount,
                    pagination.totalPage(),
                    pagination.hasPrev(),
                    pagination.hasNext(),
                    pagination.prevPage(),
                    pagination.nextPage(),
                    pagination.startPage(),
                    pagination.endPage(),
                    pagination.pageNumbers(),
                    category,
                    "result".equals(category),
                    "upcoming".equals(category),
                    "past".equals(category));
        }

        private static PaginationMeta createPagination(int currentPage, long totalCount, int size) {
            int totalPage = (int) Math.ceil((double) totalCount / size);
            int startPage = (currentPage / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE;
            int endPage = totalPage == 0 ? -1 : Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPage - 1);

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

    // 여행 계획 목록 카드 응답 DTO
    @Data
    public static class PlanSummaryDTO {
        private Integer id;
        private String title;
        private String imgUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private String placeName;
        private String dDay;
        private boolean disabled;

        public PlanSummaryDTO(TripPlan tripPlan,
                String imgUrl,
                String placeName,
                String dDay,
                boolean disabled) {
            this.id = tripPlan.getId();
            this.title = tripPlan.getTitle();
            this.imgUrl = imgUrl;
            this.startDate = tripPlan.getStartDate();
            this.endDate = tripPlan.getEndDate();
            this.placeName = placeName;
            this.dDay = dDay;
            this.disabled = disabled;
        }
    }

    // 여행 장소 단건 응답 DTO
    @Data
    public static class PlaceDTO {
        private Integer id;
        private String placeName;
        private String address;
        private Integer dayOrder;

        public PlaceDTO(TripPlace tripPlace) {
            this.id = tripPlace.getId();
            this.placeName = tripPlace.getPlaceName();
            this.address = tripPlace.getAddress();
            this.dayOrder = tripPlace.getDayOrder();
        }
    }

    // 여행 계획 상세 응답 DTO
    @Data
    public static class PlanDetailDTO {
        private Integer id;
        private String title;
        private String region;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<PlaceDTO> places;

        public PlanDetailDTO(TripPlan tripPlan, String region, List<PlaceDTO> places) {
            this.id = tripPlan.getId();
            this.title = tripPlan.getTitle();
            this.region = region;
            this.startDate = tripPlan.getStartDate();
            this.endDate = tripPlan.getEndDate();
            this.places = places;
        }

        public String getFormattedTitle() {
            if (region == null || region.trim().isEmpty()) {
                if (title == null || title.trim().isEmpty()) {
                    return "여행";
                }
                return title.split(" ")[0] + " 여행";
            }
            return region + " 여행";
        }
    }

    // 페이지 번호 DTO
    @Data
    @AllArgsConstructor
    public static class PageNumberDTO {
        private int page;
        private int displayPage;
        private boolean current;
    }
}
