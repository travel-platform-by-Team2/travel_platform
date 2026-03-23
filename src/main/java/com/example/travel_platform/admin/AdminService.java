package com.example.travel_platform.admin;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardCategory;
import com.example.travel_platform.board.BoardLikeRepository;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int DASHBOARD_RECENT_LIMIT = 3;
    private static final int RECENT_BOARD_DAYS = 7;
    private static final int BOARD_PAGE_SIZE = 10;
    private static final int BOARD_PAGE_BLOCK_SIZE = 5;
    private static final DateTimeFormatter DASHBOARD_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy.MM.dd HH:mm");
    private static final String USER_SORT_BY_CREATED_AT = "createdAt";
    private static final String USER_SORT_BY_POST_COUNT = "postCount";
    private static final String USER_ORDER_BY_ASC = "asc";
    private static final String USER_ORDER_BY_DESC = "desc";

    private final AdminQueryRepository adminQueryRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;

    public AdminResponse.DashboardViewDTO getDashboardView() {
        long totalUserCount = adminQueryRepository.countUsers();
        long inactiveUserCount = adminQueryRepository.countInactiveUsers();
        long activeUserCount = totalUserCount - inactiveUserCount;

        long totalBoardCount = adminQueryRepository.countBoards();
        long recentBoardCount = adminQueryRepository
                .countBoardsByCreatedAtAfter(LocalDateTime.now().minusDays(RECENT_BOARD_DAYS));
        long totalBoardViewCount = adminQueryRepository.sumBoardViewCount();

        return AdminResponse.DashboardViewDTO.createDashboardView(
                totalUserCount,
                activeUserCount,
                inactiveUserCount,
                totalBoardCount,
                recentBoardCount,
                totalBoardViewCount,
                createDashboardMetrics(totalUserCount, activeUserCount, inactiveUserCount, totalBoardCount,
                        recentBoardCount,
                        totalBoardViewCount),
                createUserStatusItems(totalUserCount, activeUserCount, inactiveUserCount),
                createBoardCategoryItems(totalBoardCount),
                loadRecentUsers(),
                loadRecentBoards());
    }

    public AdminResponse.UserListViewDTO getUserListView(Boolean active, String keyword, String sortBy,
            String orderBy) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSortBy = normalizeUserSortBy(sortBy);
        String normalizedOrderBy = normalizeUserOrderBy(orderBy);
        List<AdminUserSummaryRow> userRows = findUsersByFilter(active, normalizedKeyword);
        List<AdminResponse.AdminUserDTO> userModels = createAdminUserModels(userRows, normalizedSortBy,
                normalizedOrderBy);
        AdminResponse.UserListPageDTO pageModel = createUserListPageModel(
                normalizedKeyword,
                active,
                normalizedSortBy,
                normalizedOrderBy);

        return AdminResponse.UserListViewDTO.createUserListView(pageModel, userModels);
    }

    @Transactional
    public void updateUserActiveStatus(Integer userId, boolean active) {
        User user = findUser(userId);
        user.setActive(active);
    }

    @Transactional
    public void deleteBoardByAdmin(SessionUser sessionUser, Integer boardId) {
        requireAdminSessionUser(sessionUser);
        Board board = findDeleteTargetBoard(boardId);
        deleteBoardLikeRelations(boardId);
        boardRepository.delete(board);
    }

    public AdminResponse.BoardListViewDTO getBoardListView(String category, String keyword, String sort, int page) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSort = normalizeBoardSort(sort);
        String selectedCategory = resolveSelectedCategory(category);
        boolean allCategoryTab = isAllCategory(selectedCategory);
        BoardCategory boardCategory = resolveBoardCategoryOrNull(selectedCategory);
        List<AdminBoardSummaryRow> boardRows = findBoards(boardCategory, normalizedKeyword, normalizedSort, page);
        long totalCount = countBoards(boardCategory, normalizedKeyword);
        List<AdminResponse.AdminBoardDTO> boardModels = createAdminBoardModels(boardRows);
        AdminResponse.BoardListPageDTO pageModel = createBoardListPageModel(
                selectedCategory,
                normalizedKeyword,
                normalizedSort,
                selectedCategory,
                allCategoryTab,
                totalCount,
                page);

        return AdminResponse.BoardListViewDTO.createBoardListView(pageModel, boardModels);
    }

    private AdminResponse.UserListPageDTO createUserListPageModel(
            String normalizedKeyword,
            Boolean active,
            String normalizedSortBy,
            String normalizedOrderBy) {
        return AdminResponse.UserListPageDTO.createUserListPage(
                adminQueryRepository.countUsers(),
                adminQueryRepository.countInactiveUsers(),
                normalizedKeyword,
                active,
                active == null,
                Boolean.TRUE.equals(active),
                Boolean.FALSE.equals(active),
                normalizedSortBy,
                normalizedOrderBy,
                buildUsersUrl(null, normalizedKeyword, normalizedSortBy, normalizedOrderBy),
                buildUsersUrl(true, normalizedKeyword, normalizedSortBy, normalizedOrderBy),
                buildUsersUrl(false, normalizedKeyword, normalizedSortBy, normalizedOrderBy));
    }

    private List<AdminUserSummaryRow> findUsersByFilter(Boolean active, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword && active != null) {
            return adminQueryRepository.findUserSummaryRowsByActiveAndKeyword(active, keyword);
        }
        if (hasKeyword) {
            return adminQueryRepository.findUserSummaryRowsByKeyword(keyword);
        }
        if (active == null) {
            return adminQueryRepository.findAllUserSummaryRowsByCreatedAtDesc();
        }
        return adminQueryRepository.findUserSummaryRowsByActiveByCreatedAtDesc(active);
    }

    private List<AdminResponse.AdminUserDTO> createAdminUserModels(
            List<AdminUserSummaryRow> userRows,
            String sortBy,
            String orderBy) {
        List<AdminResponse.AdminUserDTO> userModels = new ArrayList<>();

        for (AdminUserSummaryRow userRow : userRows) {
            userModels.add(AdminResponse.AdminUserDTO.createAdminUser(
                    userRow.userId(),
                    userRow.username(),
                    userRow.email(),
                    userRow.createdAt().toLocalDate(),
                    userRow.active(),
                    Math.toIntExact(userRow.boardCount())));
        }

        userModels.sort(buildUserSortComparator(sortBy, orderBy));
        return userModels;
    }

    private Comparator<AdminResponse.AdminUserDTO> buildUserSortComparator(String sortBy, String orderBy) {
        return (left, right) -> compareAdminUsers(left, right, sortBy, orderBy);
    }

    private int compareAdminUsers(
            AdminResponse.AdminUserDTO left,
            AdminResponse.AdminUserDTO right,
            String sortBy,
            String orderBy) {
        int compareResult;

        if (USER_SORT_BY_POST_COUNT.equals(sortBy)) {
            compareResult = Integer.compare(left.getBoardCount(), right.getBoardCount());
        } else {
            compareResult = left.getCreatedAt().compareTo(right.getCreatedAt());
        }

        if (USER_ORDER_BY_DESC.equals(orderBy)) {
            compareResult = compareResult * -1;
        }

        if (compareResult != 0) {
            return compareResult;
        }

        return Integer.compare(left.getUserId(), right.getUserId());
    }

    private AdminResponse.BoardListPageDTO createBoardListPageModel(
            String category,
            String normalizedKeyword,
            String normalizedSort,
            String selectedCategory,
            boolean allCategoryTab,
            long totalCount,
            int page) {
        int totalPages = resolveBoardTotalPages(totalCount);

        return AdminResponse.BoardListPageDTO.createBoardListPage(
                createBoardPageItems(page, totalPages, normalizedKeyword, normalizedSort, selectedCategory),
                page,
                totalPages,
                totalCount,
                adminQueryRepository.countBoards(),
                getPrevPage(page),
                getNextPage(page, totalPages),
                category,
                normalizedKeyword,
                normalizedSort,
                toSortFieldLabel(normalizedSort),
                toSortDirectionLabel(normalizedSort),
                toToggleDirectionSort(normalizedSort),
                toFieldSort(normalizedSort, "date"),
                toFieldSort(normalizedSort, "view"),
                toFieldSort(normalizedSort, "likes"),
                selectedCategory,
                allCategoryTab,
                allCategoryTab ? null : selectedCategory,
                isCategory(category, "tips"),
                isCategory(category, "plan"),
                isCategory(category, "food"),
                isCategory(category, "review"),
                isCategory(category, "qna"));
    }

    private List<AdminBoardSummaryRow> findBoards(BoardCategory selectedCategory, String keyword, String sort,
            int page) {
        int offset = page * BOARD_PAGE_SIZE;
        String[] words = keyword.isBlank() ? new String[0] : splitKeywords(keyword);
        return adminQueryRepository.findBoardSummaryRows(selectedCategory, words, sort, offset, BOARD_PAGE_SIZE);
    }

    private long countBoards(BoardCategory selectedCategory, String keyword) {
        String[] words = keyword.isBlank() ? new String[0] : splitKeywords(keyword);
        return adminQueryRepository.countBoardSummaryRows(selectedCategory, words);
    }

    private List<AdminResponse.PageItemDTO> createBoardPageItems(
            int page,
            int totalPages,
            String keyword,
            String sort,
            String selectedCategory) {
        int startPage = (page / BOARD_PAGE_BLOCK_SIZE) * BOARD_PAGE_BLOCK_SIZE;
        int endPage = Math.min(startPage + BOARD_PAGE_BLOCK_SIZE - 1, totalPages - 1);
        List<AdminResponse.PageItemDTO> pageItems = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            pageItems.add(
                    AdminResponse.PageItemDTO.createPageItem(i, i + 1, i == page, keyword, sort, selectedCategory));
        }

        return pageItems;
    }

    private List<AdminResponse.AdminBoardDTO> createAdminBoardModels(List<AdminBoardSummaryRow> boardRows) {
        List<AdminResponse.AdminBoardDTO> boardModels = new ArrayList<>();

        for (AdminBoardSummaryRow boardRow : boardRows) {
            boardModels.add(AdminResponse.AdminBoardDTO.createAdminBoard(
                    boardRow.boardId(),
                    boardRow.title(),
                    boardRow.userName(),
                    boardRow.createdAt().toLocalDate(),
                    boardRow.viewCount(),
                    boardRow.category().getLabel(),
                    boardRow.category().getCssClass()));
        }

        return boardModels;
    }

    private int resolveBoardTotalPages(long totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / BOARD_PAGE_SIZE);
        return totalPages == 0 ? 1 : totalPages;
    }

    private List<AdminResponse.DashboardMetricDTO> createDashboardMetrics(
            long totalUserCount,
            long activeUserCount,
            long inactiveUserCount,
            long totalBoardCount,
            long recentBoardCount,
            long totalBoardViewCount) {
        List<AdminResponse.DashboardMetricDTO> metrics = new ArrayList<>();
        metrics.add(AdminResponse.DashboardMetricDTO.createMetric("전체 사용자", formatCount(totalUserCount),
                "활성 " + formatCount(activeUserCount) + "명", "groups", "metric-icon-blue"));
        metrics.add(AdminResponse.DashboardMetricDTO.createMetric("비활성 사용자", formatCount(inactiveUserCount),
                "관리 필요한 계정", "person_off", "metric-icon-rose"));
        metrics.add(AdminResponse.DashboardMetricDTO.createMetric("전체 게시글", formatCount(totalBoardCount),
                "최근 7일 " + formatCount(recentBoardCount) + "개", "forum", "metric-icon-purple"));
        metrics.add(AdminResponse.DashboardMetricDTO.createMetric("누적 조회수", formatCount(totalBoardViewCount),
                "커뮤니티 전체 기준", "visibility", "metric-icon-orange"));
        return metrics;
    }

    private List<AdminResponse.StatusChartItemDTO> createUserStatusItems(
            long totalUserCount,
            long activeUserCount,
            long inactiveUserCount) {
        List<AdminResponse.StatusChartItemDTO> items = new ArrayList<>();
        items.add(createUserStatusItem("활성 사용자", activeUserCount, totalUserCount, "status-active",
                "admin-chart-bar--blue"));
        items.add(createUserStatusItem("비활성 사용자", inactiveUserCount, totalUserCount, "status-danger",
                "admin-chart-bar--rose"));
        return items;
    }

    private AdminResponse.StatusChartItemDTO createUserStatusItem(
            String label,
            long count,
            long total,
            String badgeClass,
            String barClass) {
        int percent = calculatePercent(count, total);
        return AdminResponse.StatusChartItemDTO.createStatusItem(
                label,
                formatCount(count) + "명",
                percent + "%",
                percent,
                badgeClass,
                barClass);
    }

    private List<AdminResponse.CategoryChartItemDTO> createBoardCategoryItems(long totalBoardCount) {
        List<AdminResponse.CategoryChartItemDTO> items = new ArrayList<>();
        items.add(createCategoryChartItem(BoardCategory.TIPS.getLabel(),
                adminQueryRepository.countBoardsByCategory(BoardCategory.TIPS), totalBoardCount,
                "admin-category-badge cat-tips", "admin-chart-bar--tips"));
        items.add(createCategoryChartItem(BoardCategory.PLAN.getLabel(),
                adminQueryRepository.countBoardsByCategory(BoardCategory.PLAN), totalBoardCount,
                "admin-category-badge cat-plan", "admin-chart-bar--plan"));
        items.add(createCategoryChartItem(BoardCategory.FOOD.getLabel(),
                adminQueryRepository.countBoardsByCategory(BoardCategory.FOOD), totalBoardCount,
                "admin-category-badge cat-food", "admin-chart-bar--food"));
        items.add(createCategoryChartItem(BoardCategory.REVIEW.getLabel(),
                adminQueryRepository.countBoardsByCategory(BoardCategory.REVIEW), totalBoardCount,
                "admin-category-badge cat-review", "admin-chart-bar--review"));
        items.add(createCategoryChartItem(BoardCategory.QNA.getLabel(),
                adminQueryRepository.countBoardsByCategory(BoardCategory.QNA), totalBoardCount,
                "admin-category-badge cat-qna", "admin-chart-bar--qna"));
        return items;
    }

    private AdminResponse.CategoryChartItemDTO createCategoryChartItem(
            String label,
            long count,
            long total,
            String badgeClass,
            String barClass) {
        int percent = calculatePercent(count, total);
        return AdminResponse.CategoryChartItemDTO.createCategoryItem(
                label,
                formatCount(count) + "개",
                percent + "%",
                percent,
                badgeClass,
                barClass);
    }

    private List<AdminResponse.RecentUserDTO> loadRecentUsers() {
        List<AdminRecentUserRow> userRows = adminQueryRepository.findRecentUserRows(DASHBOARD_RECENT_LIMIT);
        List<AdminResponse.RecentUserDTO> recentUsers = new ArrayList<>();

        for (AdminRecentUserRow userRow : userRows) {
            recentUsers.add(AdminResponse.RecentUserDTO.createRecentUser(
                    userRow.username(),
                    userRow.active() ? "활성" : "비활성",
                    userRow.active() ? "status-active" : "status-danger",
                    formatDateTime(userRow.createdAt())));
        }

        return recentUsers;
    }

    private List<AdminResponse.RecentBoardDTO> loadRecentBoards() {
        List<AdminRecentBoardRow> boardRows = adminQueryRepository.findRecentBoardRows(DASHBOARD_RECENT_LIMIT);
        List<AdminResponse.RecentBoardDTO> recentBoards = new ArrayList<>();

        for (AdminRecentBoardRow boardRow : boardRows) {
            recentBoards.add(AdminResponse.RecentBoardDTO.createRecentBoard(
                    boardRow.boardId(),
                    boardRow.title(),
                    boardRow.userName(),
                    formatDateTime(boardRow.createdAt())));
        }

        return recentBoards;
    }

    private void requireAdminSessionUser(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        if (!sessionUser.isAdmin()) {
            throw new Exception403("관리자만 사용할 수 있습니다.");
        }
    }

    private Board findDeleteTargetBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    private void deleteBoardLikeRelations(Integer boardId) {
        boardLikeRepository.deleteByBoardId(boardId);
    }

    private int calculatePercent(long count, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((double) count * 100 / total);
    }

    private String formatCount(long value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DASHBOARD_DATE_TIME_FORMATTER);
    }

    private Integer getPrevPage(int currentPage) {
        if (currentPage == 0) {
            return null;
        }
        return currentPage - 1;
    }

    private Integer getNextPage(int currentPage, int totalPages) {
        if (currentPage >= totalPages - 1) {
            return null;
        }
        return currentPage + 1;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String normalizeUserSortBy(String sortBy) {
        return USER_SORT_BY_POST_COUNT.equals(sortBy) ? USER_SORT_BY_POST_COUNT : USER_SORT_BY_CREATED_AT;
    }

    private String normalizeUserOrderBy(String orderBy) {
        return USER_ORDER_BY_ASC.equals(orderBy) ? USER_ORDER_BY_ASC : USER_ORDER_BY_DESC;
    }

    private String normalizeBoardSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "latest";
        }

        return switch (sort) {
            case "likes", "downlikes", "view", "downview", "latest", "date" -> sort;
            default -> "latest";
        };
    }

    private String resolveSelectedCategory(String category) {
        BoardCategory boardCategory = BoardCategory.fromCodeOrNull(category);
        if (boardCategory == null) {
            return "all";
        }
        return boardCategory.getCode();
    }

    private BoardCategory resolveBoardCategoryOrNull(String category) {
        if (isAllCategory(category)) {
            return null;
        }
        return BoardCategory.fromCodeOrNull(category);
    }

    private boolean isAllCategory(String category) {
        return "all".equals(category);
    }

    private String[] splitKeywords(String keyword) {
        return keyword.split("\\s+");
    }

    private String toSortFieldLabel(String sort) {
        return switch (sort) {
            case "view", "downview" -> "조회수";
            case "likes", "downlikes" -> "좋아요수";
            default -> "날짜순";
        };
    }

    private String toSortDirectionLabel(String sort) {
        return switch (sort) {
            case "downlikes", "downview", "date" -> "오름차순";
            default -> "내림차순";
        };
    }

    private String toToggleDirectionSort(String sort) {
        return switch (sort) {
            case "latest" -> "date";
            case "date" -> "latest";
            case "view" -> "downview";
            case "downview" -> "view";
            case "likes" -> "downlikes";
            case "downlikes" -> "likes";
            default -> "date";
        };
    }

    private String toFieldSort(String sort, String field) {
        boolean ascending = isAscendingSort(sort);

        return switch (field) {
            case "date" -> ascending ? "date" : "latest";
            case "view" -> ascending ? "downview" : "view";
            case "likes" -> ascending ? "downlikes" : "likes";
            default -> "latest";
        };
    }

    private boolean isAscendingSort(String sort) {
        return switch (sort) {
            case "downlikes", "downview", "date" -> true;
            default -> false;
        };
    }

    private boolean isCategory(String category, String targetCategory) {
        return targetCategory.equals(category);
    }

    private String buildUsersUrl(Boolean active, String keyword, String sortBy, String orderBy) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/users");
        if (active != null) {
            builder.queryParam("active", active);
        }
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword.trim());
        }
        if (sortBy != null && !sortBy.isBlank()) {
            builder.queryParam("sortBy", sortBy);
        }
        if (orderBy != null && !orderBy.isBlank()) {
            builder.queryParam("orderBy", orderBy);
        }
        return builder.toUriString();
    }
}
