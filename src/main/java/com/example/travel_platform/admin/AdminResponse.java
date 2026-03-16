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
        private Integer prevPage;
        private Integer nextPage;
        private String category;
    }

    @Data
    @Builder
    public static class PageItemDTO {
        private int page;
        private int displayNumber;
        private boolean current;
    }
}
