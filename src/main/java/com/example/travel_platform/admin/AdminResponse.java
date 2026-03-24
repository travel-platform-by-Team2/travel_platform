package com.example.travel_platform.admin;

import java.time.LocalDate;
import java.util.List;

import com.example.travel_platform.board.BoardSort;

import lombok.Data;

public class AdminResponse {

    private static final String DASHBOARD_MENU = "dashboard";
    private static final String USERS_MENU = "users";
    private static final String BOARDS_MENU = "boards";

    private static String activeClass(String currentMenu, String targetMenu) {
        if (targetMenu.equals(currentMenu)) {
            return " is-active";
        }
        return "";
    }

    private static String sortClass(BoardSort currentSort, BoardSort targetSort) {
        if (currentSort == targetSort) {
            return " is-active";
        }
        return "";
    }

    @Data
    public static class DashboardViewDTO {
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
        private String dashboardClass;
        private String usersClass;
        private String boardsClass;

        public static DashboardViewDTO createDashboardView(
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
            DashboardViewDTO dto = new DashboardViewDTO();
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
            dto.setDashboardClass(activeClass(DASHBOARD_MENU, DASHBOARD_MENU));
            dto.setUsersClass(activeClass(DASHBOARD_MENU, USERS_MENU));
            dto.setBoardsClass(activeClass(DASHBOARD_MENU, BOARDS_MENU));
            return dto;
        }
    }

    @Data
    public static class UserListPageDTO {
        private long totalUserCount;
        private long inactiveUserCount;
        private String keyword;
        private Boolean currentActive;
        private boolean allTab;
        private boolean activeTab;
        private boolean inactiveTab;
        private String dashboardClass;
        private String usersClass;
        private String boardsClass;
        private String sortBy;
        private String orderBy;
        private boolean sortByPostCount;
        private boolean sortByCreatedAt;
        private boolean orderByAsc;
        private boolean orderByDesc;
        private String allTabHref;
        private String activeTabHref;
        private String inactiveTabHref;

        public static UserListPageDTO createUserListPage(
                long totalUserCount,
                long inactiveUserCount,
                String keyword,
                Boolean currentActive,
                boolean allTab,
                boolean activeTab,
                boolean inactiveTab,
                String sortBy,
                String orderBy,
                String allTabHref,
                String activeTabHref,
                String inactiveTabHref) {
            UserListPageDTO dto = new UserListPageDTO();
            dto.setTotalUserCount(totalUserCount);
            dto.setInactiveUserCount(inactiveUserCount);
            dto.setKeyword(keyword);
            dto.setCurrentActive(currentActive);
            dto.setAllTab(allTab);
            dto.setActiveTab(activeTab);
            dto.setInactiveTab(inactiveTab);
            dto.setSortBy(sortBy);
            dto.setOrderBy(orderBy);
            dto.setSortByPostCount("postCount".equals(sortBy));
            dto.setSortByCreatedAt("createdAt".equals(sortBy));
            dto.setOrderByAsc("asc".equals(orderBy));
            dto.setOrderByDesc("desc".equals(orderBy));
            dto.setAllTabHref(allTabHref);
            dto.setActiveTabHref(activeTabHref);
            dto.setInactiveTabHref(inactiveTabHref);
            dto.setDashboardClass(activeClass(USERS_MENU, DASHBOARD_MENU));
            dto.setUsersClass(activeClass(USERS_MENU, USERS_MENU));
            dto.setBoardsClass(activeClass(USERS_MENU, BOARDS_MENU));
            return dto;
        }
    }

    @Data
    public static class UserListViewDTO {
        private UserListPageDTO model;
        private List<AdminUserDTO> models;

        public static UserListViewDTO createUserListView(UserListPageDTO model, List<AdminUserDTO> models) {
            UserListViewDTO dto = new UserListViewDTO();
            dto.setModel(model);
            dto.setModels(models);
            return dto;
        }
    }

    @Data
    public static class DashboardMetricDTO {
        private String label;
        private String valueLabel;
        private String helperLabel;
        private String icon;
        private String iconClass;

        public static DashboardMetricDTO createMetric(
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

        public static StatusChartItemDTO createStatusItem(
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

        public static CategoryChartItemDTO createCategoryItem(
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

        public static RecentUserDTO createRecentUser(
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

        public static RecentBoardDTO createRecentBoard(
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
        private LocalDate createdAt;
        private boolean active;
        private int boardCount;

        public static AdminUserDTO createAdminUser(
                Integer userId,
                String username,
                String email,
                LocalDate createdAt,
                boolean active,
                int boardCount) {
            AdminUserDTO dto = new AdminUserDTO();
            dto.setUserId(userId);
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setCreatedAt(createdAt);
            dto.setActive(active);
            dto.setBoardCount(boardCount);
            return dto;
        }
    }

    @Data
    public static class BoardListPageDTO {
        private List<PageItemDTO> pageItems;
        private int currentPage;
        private int totalPages;
        private long totalCount;
        private long allCount;
        private Integer prevPage;
        private Integer nextPage;
        private String keyword;
        private String sort;
        private String sortLabel;
        private String sortFieldLabel;
        private String sortDirectionLabel;
        private String toggleDirectionSort;
        private String likeClass;
        private String lowLikeClass;
        private String viewClass;
        private String lowViewClass;
        private String latestClass;
        private String dateClass;
        private String allCategory;
        private boolean allCategoryTab;
        private String selectCategory;
        private boolean isTips;
        private boolean isPlan;
        private boolean isFood;
        private boolean isReview;
        private boolean isQna;
        private String dashboardClass;
        private String usersClass;
        private String boardsClass;

        public static BoardListPageDTO createBoardListPage(
                List<PageItemDTO> pageItems,
                int currentPage,
                int totalPages,
                long totalCount,
                long allCount,
                Integer prevPage,
                Integer nextPage,
                String keyword,
                String sort,
                String sortFieldLabel,
                String sortDirectionLabel,
                String toggleDirectionSort,
                String allCategory,
                boolean allCategoryTab,
                String selectCategory,
                boolean isTips,
                boolean isPlan,
                boolean isFood,
                boolean isReview,
                boolean isQna) {
            BoardSort boardSort = BoardSort.fromCodeOrDefault(sort);
            BoardListPageDTO dto = new BoardListPageDTO();
            dto.setPageItems(pageItems);
            dto.setCurrentPage(currentPage);
            dto.setTotalPages(totalPages);
            dto.setTotalCount(totalCount);
            dto.setAllCount(allCount);
            dto.setPrevPage(prevPage);
            dto.setNextPage(nextPage);
            dto.setKeyword(keyword);
            dto.setSort(boardSort.getCode());
            dto.setSortLabel(boardSort.getLabel());
            dto.setSortFieldLabel(sortFieldLabel);
            dto.setSortDirectionLabel(sortDirectionLabel);
            dto.setToggleDirectionSort(toggleDirectionSort);
            dto.setLikeClass(sortClass(boardSort, BoardSort.LIKES));
            dto.setLowLikeClass(sortClass(boardSort, BoardSort.DOWNLIKES));
            dto.setViewClass(sortClass(boardSort, BoardSort.VIEW));
            dto.setLowViewClass(sortClass(boardSort, BoardSort.DOWNVIEW));
            dto.setLatestClass(sortClass(boardSort, BoardSort.LATEST));
            dto.setDateClass(sortClass(boardSort, BoardSort.DATE));
            dto.setAllCategory(allCategory);
            dto.setAllCategoryTab(allCategoryTab);
            dto.setSelectCategory(selectCategory);
            dto.setTips(isTips);
            dto.setPlan(isPlan);
            dto.setFood(isFood);
            dto.setReview(isReview);
            dto.setQna(isQna);
            dto.setDashboardClass(activeClass(BOARDS_MENU, DASHBOARD_MENU));
            dto.setUsersClass(activeClass(BOARDS_MENU, USERS_MENU));
            dto.setBoardsClass(activeClass(BOARDS_MENU, BOARDS_MENU));
            return dto;
        }
    }

    @Data
    public static class BoardListViewDTO {
        private BoardListPageDTO model;
        private List<AdminBoardDTO> models;

        public static BoardListViewDTO createBoardListView(BoardListPageDTO model, List<AdminBoardDTO> models) {
            BoardListViewDTO dto = new BoardListViewDTO();
            dto.setModel(model);
            dto.setModels(models);
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

        public static AdminBoardDTO createAdminBoard(
                Integer id,
                String title,
                String userName,
                LocalDate createdDate,
                Integer viewCount,
                String category,
                String categoryClass) {
            AdminBoardDTO dto = new AdminBoardDTO();
            dto.setId(id);
            dto.setTitle(title);
            dto.setUserName(userName);
            dto.setCreatedDate(createdDate);
            dto.setViewCount(viewCount);
            dto.setCategory(category);
            dto.setCategoryClass(categoryClass);
            return dto;
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

        public static PageItemDTO createPageItem(
                int page,
                int displayNumber,
                boolean current,
                String keyword,
                String sort,
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
