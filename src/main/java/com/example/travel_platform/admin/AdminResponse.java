package com.example.travel_platform.admin;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

public class AdminResponse {

    @Data
    @Builder
    public static class AdminBoardDTO {
        private Integer id;
        private String title;
        private String userName;
        private LocalDate startDate;
        private Integer viewCount;

        private String category;
        private String categoryClass;
    }

    @Data
    @Builder
    public static class AdminBoardListDTO {
        private List<AdminBoardDTO> boards;
        private List<PageItemDTO> pageItems;
        private int currentPage;
        private int totalPages;
        private long totalCount;
        private long allCount;
        private Integer prevPage;
        private Integer nextPage;
        private String category;

        private String keyword;
        private String sort;
        private String sortLabel;
        private String allCategory;
        private boolean isSortLikes;
        private boolean isSortDownlikes;
        private boolean isSortViews;
        private boolean isSortDownviews;
        private boolean isSortLatest;
        private boolean isSortDate;
        private boolean isTips;
        private boolean isPlan;
        private boolean isFood;
        private boolean isReview;
        private boolean isQna;
    }

    @Data
    @Builder
    public static class PageItemDTO {
        private int page;
        private int displayNumber;
        private boolean current;
        private String keyword;
        private String sort;
        private String selectCategory;
    }
}
