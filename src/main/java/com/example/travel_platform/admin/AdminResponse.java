package com.example.travel_platform.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

public class AdminResponse {

    private static String activeClass(String currentMenu, String targetMenu) {
        if (targetMenu.equals(currentMenu)) {
            return " is-active";
        }
        return "";
    }

    @Data
    public static class UserListPageDTO {
        private List<AdminUserDTO> users;
        private long totalUserCount;
        private long inactiveUserCount;
        private String keyword;
        private Boolean currentActive;
        private boolean allTab;
        private boolean activeTab;
        private boolean inactiveTab;
        private String dashboardActiveClass;
        private String usersActiveClass;
        private String boardsActiveClass;

        public static UserListPageDTO of(
                List<AdminUserDTO> users,
                long totalUserCount,
                long inactiveUserCount,
                String keyword,
                Boolean currentActive,
                boolean allTab,
                boolean activeTab,
                boolean inactiveTab) {
            UserListPageDTO dto = new UserListPageDTO();
            dto.setUsers(users);
            dto.setTotalUserCount(totalUserCount);
            dto.setInactiveUserCount(inactiveUserCount);
            dto.setKeyword(keyword);
            dto.setCurrentActive(currentActive);
            dto.setAllTab(allTab);
            dto.setActiveTab(activeTab);
            dto.setInactiveTab(inactiveTab);
            return dto;
        }

        public UserListPageDTO withCurrentMenu(String currentMenu) {
            this.dashboardActiveClass = activeClass(currentMenu, "dashboard");
            this.usersActiveClass = activeClass(currentMenu, "users");
            this.boardsActiveClass = activeClass(currentMenu, "boards");
            return this;
        }
    }

    @Data
    public static class DashboardPageDTO {
        private long totalUserCount;
        private long activeUserCount;
        private long inactiveUserCount;
        private long totalBoardCount;
        private long recentBoardCount;
        private long totalBoardViewCount;
        private List<DashboardMetricDTO> metrics;
        private List<StatusChartItemDTO> userStatusItems;
        private List<CategoryChartItemDTO> boardCategoryItems;
        private List<RecentUserDTO> recentUsers;
        private List<RecentBoardDTO> recentBoards;
        private boolean hasRecentUsers;
        private boolean hasRecentBoards;
        private String dashboardActiveClass;
        private String usersActiveClass;
        private String boardsActiveClass;

        public static DashboardPageDTO of(
                long totalUserCount,
                long activeUserCount,
                long inactiveUserCount,
                long totalBoardCount,
                long recentBoardCount,
                long totalBoardViewCount,
                List<DashboardMetricDTO> metrics,
                List<StatusChartItemDTO> userStatusItems,
                List<CategoryChartItemDTO> boardCategoryItems,
                List<RecentUserDTO> recentUsers,
                List<RecentBoardDTO> recentBoards) {
            DashboardPageDTO dto = new DashboardPageDTO();
            dto.setTotalUserCount(totalUserCount);
            dto.setActiveUserCount(activeUserCount);
            dto.setInactiveUserCount(inactiveUserCount);
            dto.setTotalBoardCount(totalBoardCount);
            dto.setRecentBoardCount(recentBoardCount);
            dto.setTotalBoardViewCount(totalBoardViewCount);
            dto.setMetrics(metrics);
            dto.setUserStatusItems(userStatusItems);
            dto.setBoardCategoryItems(boardCategoryItems);
            dto.setRecentUsers(recentUsers);
            dto.setRecentBoards(recentBoards);
            dto.setHasRecentUsers(!recentUsers.isEmpty());
            dto.setHasRecentBoards(!recentBoards.isEmpty());
            return dto;
        }

        public DashboardPageDTO withCurrentMenu(String currentMenu) {
            this.dashboardActiveClass = activeClass(currentMenu, "dashboard");
            this.usersActiveClass = activeClass(currentMenu, "users");
            this.boardsActiveClass = activeClass(currentMenu, "boards");
            return this;
        }
    }

    @Data
    public static class DashboardMetricDTO {
        private String label;
        private String valueLabel;
        private String helperLabel;
        private String icon;
        private String iconClass;

        public static DashboardMetricDTO of(
                String label,
                String valueLabel,
                String helperLabel,
                String icon,
                String iconClass) {
            DashboardMetricDTO dto = new DashboardMetricDTO();
            dto.setLabel(label);
            dto.setValueLabel(valueLabel);
            dto.setHelperLabel(helperLabel);
            dto.setIcon(icon);
            dto.setIconClass(iconClass);
            return dto;
        }
    }

    @Data
    public static class StatusChartItemDTO {
        private String label;
        private String countLabel;
        private String percentLabel;
        private int barWidth;
        private String badgeClass;
        private String barClass;

        public static StatusChartItemDTO of(
                String label,
                String countLabel,
                String percentLabel,
                int barWidth,
                String badgeClass,
                String barClass) {
            StatusChartItemDTO dto = new StatusChartItemDTO();
            dto.setLabel(label);
            dto.setCountLabel(countLabel);
            dto.setPercentLabel(percentLabel);
            dto.setBarWidth(barWidth);
            dto.setBadgeClass(badgeClass);
            dto.setBarClass(barClass);
            return dto;
        }
    }

    @Data
    public static class CategoryChartItemDTO {
        private String label;
        private String countLabel;
        private String percentLabel;
        private int barWidth;
        private String badgeClass;
        private String barClass;

        public static CategoryChartItemDTO of(
                String label,
                String countLabel,
                String percentLabel,
                int barWidth,
                String badgeClass,
                String barClass) {
            CategoryChartItemDTO dto = new CategoryChartItemDTO();
            dto.setLabel(label);
            dto.setCountLabel(countLabel);
            dto.setPercentLabel(percentLabel);
            dto.setBarWidth(barWidth);
            dto.setBadgeClass(badgeClass);
            dto.setBarClass(barClass);
            return dto;
        }
    }

    @Data
    public static class RecentUserDTO {
        private String username;
        private String statusText;
        private String statusClass;
        private String createdAtLabel;

        public static RecentUserDTO of(
                String username,
                String statusText,
                String statusClass,
                String createdAtLabel) {
            RecentUserDTO dto = new RecentUserDTO();
            dto.setUsername(username);
            dto.setStatusText(statusText);
            dto.setStatusClass(statusClass);
            dto.setCreatedAtLabel(createdAtLabel);
            return dto;
        }
    }

    @Data
    public static class RecentBoardDTO {
        private Integer id;
        private String title;
        private String userName;
        private String createdAtLabel;

        public static RecentBoardDTO of(
                Integer id,
                String title,
                String userName,
                String createdAtLabel) {
            RecentBoardDTO dto = new RecentBoardDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setUserName(userName);
            dto.setCreatedAtLabel(createdAtLabel);
            return dto;
        }
    }

    @Data
    public static class AdminUserDTO {
        private Integer userId;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private boolean active;
        private String statusText;
        private String managementLabel;

        public static AdminUserDTO of(
                Integer userId,
                String username,
                String email,
                LocalDateTime createdAt,
                boolean active,
                String statusText,
                String managementLabel) {
            AdminUserDTO dto = new AdminUserDTO();
            dto.setUserId(userId);
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setCreatedAt(createdAt);
            dto.setActive(active);
            dto.setStatusText(statusText);
            dto.setManagementLabel(managementLabel);
            return dto;
        }
    }

    @Data
    public static class AdminBoardDTO {
        private Integer id;
        private String title;
        private String userName;
        private LocalDate createdDate;
        private Integer viewCount;

        private String category;
        private String categoryClass;

        public static AdminBoardDTO of(
                Integer id,
                String title,
                String userName,
                LocalDate startDate,
                Integer viewCount,
                String category,
                String categoryClass) {
            AdminBoardDTO dto = new AdminBoardDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setUserName(userName);
            dto.setCreatedDate(startDate);
            dto.setViewCount(viewCount);
            dto.setCategory(category);
            dto.setCategoryClass(categoryClass);
            return dto;
        }
    }

    @Data
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
        private String sortFieldLabel;
        private String sortDirectionLabel;
        private String toggleDirectionSort;
        private String dateField;
        private String viewField;
        private String likesField;
        private String allCategory;
        private boolean allCategoryTab;
        private String selectCategory;
        private boolean isTips;
        private boolean isPlan;
        private boolean isFood;
        private boolean isReview;
        private boolean isQna;
        private String dashboardActiveClass;
        private String usersActiveClass;
        private String boardsActiveClass;

        public static AdminBoardListDTO of(
                List<AdminBoardDTO> boards,
                List<PageItemDTO> pageItems,
                int currentPage,
                int totalPages,
                long totalCount,
                long allCount,
                Integer prevPage,
                Integer nextPage,
                String category,
                String keyword,
                String sort,
                String sortFieldLabel,
                String sortDirectionLabel,
                String toggleDirectionSort,
                String dateField,
                String viewField,
                String likesField,
                String allCategory,
                boolean allCategoryTab,
                String selectCategory,
                boolean isTips,
                boolean isPlan,
                boolean isFood,
                boolean isReview,
                boolean isQna) {
            AdminBoardListDTO dto = new AdminBoardListDTO();
            dto.setBoards(boards);
            dto.setPageItems(pageItems);
            dto.setCurrentPage(currentPage);
            dto.setTotalPages(totalPages);
            dto.setTotalCount(totalCount);
            dto.setAllCount(allCount);
            dto.setPrevPage(prevPage);
            dto.setNextPage(nextPage);
            dto.setCategory(category);
            dto.setKeyword(keyword);
            dto.setSort(sort);
            dto.setSortFieldLabel(sortFieldLabel);
            dto.setSortDirectionLabel(sortDirectionLabel);
            dto.setToggleDirectionSort(toggleDirectionSort);
            dto.setDateField(dateField);
            dto.setViewField(viewField);
            dto.setLikesField(likesField);
            dto.setAllCategory(allCategory);
            dto.setAllCategoryTab(allCategoryTab);
            dto.setSelectCategory(selectCategory);
            dto.setTips(isTips);
            dto.setPlan(isPlan);
            dto.setFood(isFood);
            dto.setReview(isReview);
            dto.setQna(isQna);
            return dto;
        }

        public AdminBoardListDTO withCurrentMenu(String currentMenu) {
            this.dashboardActiveClass = activeClass(currentMenu, "dashboard");
            this.usersActiveClass = activeClass(currentMenu, "users");
            this.boardsActiveClass = activeClass(currentMenu, "boards");
            return this;
        }
    }

    @Data
    public static class PageItemDTO {
        private int page;
        private int displayNumber;
        private boolean current;
        private String keyword;
        private String sort;
        private String selectCategory;

        public static PageItemDTO of(int page, int displayNumber, boolean current, String keyword, String sort,
                String selectCategory) {
            PageItemDTO dto = new PageItemDTO();
            dto.setPage(page);
            dto.setDisplayNumber(displayNumber);
            dto.setCurrent(current);
            dto.setKeyword(keyword);
            dto.setSort(sort);
            dto.setSelectCategory(selectCategory);
            return dto;
        }
    }
}
