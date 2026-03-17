package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// 여행 계획/장소 조회 응답 포맷을 정의하는 DTO 클래스
public class TripResponse {

    @Data
    @Builder
    public static class PlanListPageDTO {
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
    }

    // 여행 계획 목록 아이템 응답 DTO
    @Data
    @Builder
    public static class PlanSummaryDTO {
        private Integer id;
        private String title;
        private String imgUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private String placeName;
        private String dDay;
        private boolean disabled;
    }

    // 여행 장소 단건 응답 DTO
    @Data
    @Builder
    public static class PlaceDTO {
        private Integer id;
        private String placeName;
        private String address;
        private Integer dayOrder;
    }

    // 여행 계획 상세 응답 DTO
    @Data
    @Builder
    public static class PlanDetailDTO {
        private Integer id;
        private String title;
        private String region;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<PlaceDTO> places;

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

    // 페이징 DTO
    @Data
    @Builder
    @AllArgsConstructor
    public static class PageNumberDTO {
        private int page;
        private int displayPage;
        private boolean current;
    }
}
