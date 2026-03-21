package com.example.travel_platform.admin;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardLikeRepository;
import com.example.travel_platform.board.BoardQueryRepository;
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
    private static final DateTimeFormatter DASHBOARD_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final String USER_SORT_BY_CREATED_AT = "createdAt";
    private static final String USER_SORT_BY_POST_COUNT = "postCount";
    private static final String USER_ORDER_BY_ASC = "asc";
    private static final String USER_ORDER_BY_DESC = "desc";

    private final AdminUserQueryRepository adminUserQueryRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardQueryRepository boardQueryRepository;
    private final BoardLikeRepository boardLikeRepository;

    public AdminResponse.DashboardViewDTO getDashboardView() {
        long totalUserCount = adminUserQueryRepository.countUsers();
        long inactiveUserCount = adminUserQueryRepository.countInactiveUsers();
        long activeUserCount = totalUserCount - inactiveUserCount;

        long totalBoardCount = boardQueryRepository.count();
        long recentBoardCount = boardQueryRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(RECENT_BOARD_DAYS));
        long totalBoardViewCount = boardQueryRepository.sumViewCount();

        return AdminResponse.DashboardViewDTO.createDashboardView(
                totalUserCount,
                activeUserCount,
                inactiveUserCount,
                totalBoardCount,
                recentBoardCount,
                totalBoardViewCount,
                createDashboardMetrics(totalUserCount, activeUserCount, inactiveUserCount, totalBoardCount, recentBoardCount,
                        totalBoardViewCount),
                createUserStatusItems(totalUserCount, activeUserCount, inactiveUserCount),
                createBoardCategoryItems(totalBoardCount),
                loadRecentUsers(),
                loadRecentBoards());
    }

    public AdminResponse.UserListViewDTO getUserListView(Boolean active, String keyword, String sortBy, String orderBy) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSortBy = normalizeUserSortBy(sortBy);
        String normalizedOrderBy = normalizeUserOrderBy(orderBy);
        List<User> users = findUsersByFilter(active, normalizedKeyword);
        List<AdminResponse.AdminUserDTO> userModels = createAdminUserModels(users, normalizedSortBy, normalizedOrderBy);
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
        List<Board> boards = findBoards(selectedCategory, normalizedKeyword, normalizedSort, page);
        long totalCount = countBoards(selectedCategory, normalizedKeyword);
        List<AdminResponse.AdminBoardDTO> boardModels = createAdminBoardModels(boards);
        AdminResponse.BoardListPageDTO pageModel = createBoardListPageModel(
                category,
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
                adminUserQueryRepository.countUsers(),
                adminUserQueryRepository.countInactiveUsers(),
                normalizedKeyword,
                active,
                active == null,
                Boolean.TRUE.equals(active),
                Boolean.FALSE.equals(active),
                normalizedSortBy,
                normalizedOrderBy);
    }

    private List<User> findUsersByFilter(Boolean active, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword && active != null) {
            return adminUserQueryRepository.findUsersByActiveAndKeyword(active, keyword);
        }
        if (hasKeyword) {
            return adminUserQueryRepository.findUsersByKeyword(keyword);
        }
        if (active == null) {
            return adminUserQueryRepository.findAllUsersByCreatedAtDesc();
        }
        return adminUserQueryRepository.findUsersByActiveByCreatedAtDesc(active);
    }

    private List<AdminResponse.AdminUserDTO> createAdminUserModels(List<User> users, String sortBy, String orderBy) {
        Map<Integer, Long> boardCounts = boardQueryRepository.countBoardsByUserIds(users.stream().map(User::getId).toList());
        List<AdminResponse.AdminUserDTO> userModels = new ArrayList<>();

        for (User user : users) {
            userModels.add(AdminResponse.AdminUserDTO.createAdminUser(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getCreatedAt().toLocalDate(),
                    user.isActive(),
                    Math.toIntExact(boardCounts.getOrDefault(user.getId(), 0L)),
                    user.isActive() ? "활성" : "비활성",
                    user.isActive() ? "비활성" : "활성"));
        }

        userModels.sort(buildUserSortComparator(sortBy, orderBy));
        return userModels;
    }

    private Comparator<AdminResponse.AdminUserDTO> buildUserSortComparator(String sortBy, String orderBy) {
        Comparator<AdminResponse.AdminUserDTO> primaryComparator = USER_SORT_BY_POST_COUNT.equals(sortBy)
                ? Comparator.comparingInt(AdminResponse.AdminUserDTO::getBoardCount)
                : Comparator.comparing(AdminResponse.AdminUserDTO::getCreatedAt);

        if (USER_ORDER_BY_DESC.equals(orderBy)) {
            primaryComparator = primaryComparator.reversed();
        }

        return primaryComparator.thenComparing(AdminResponse.AdminUserDTO::getUserId);
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
                boardQueryRepository.count(),
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

    private List<Board> findBoards(String selectedCategory, String keyword, String sort, int page) {
        int offset = page * BOARD_PAGE_SIZE;
        boolean hasKeyword = !keyword.isBlank();

        if (hasKeyword) {
            String[] words = splitKeywords(keyword);
            if (isAllCategory(selectedCategory)) {
                return boardQueryRepository.search(null, words, sort, offset, BOARD_PAGE_SIZE);
            }
            return boardQueryRepository.search(selectedCategory, words, sort, offset, BOARD_PAGE_SIZE);
        }

        if (isAllCategory(selectedCategory)) {
            return boardQueryRepository.findAllPaging(sort, offset, BOARD_PAGE_SIZE);
        }

        return boardQueryRepository.findAllPagingByCategory(selectedCategory, sort, offset, BOARD_PAGE_SIZE);
    }

    private long countBoards(String selectedCategory, String keyword) {
        boolean hasKeyword = !keyword.isBlank();

        if (hasKeyword) {
            String[] words = splitKeywords(keyword);
            if (isAllCategory(selectedCategory)) {
                return boardQueryRepository.countSearch(null, words);
            }
            return boardQueryRepository.countSearch(selectedCategory, words);
        }

        if (isAllCategory(selectedCategory)) {
            return boardQueryRepository.count();
        }

        return boardQueryRepository.countByCategory(selectedCategory);
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
            pageItems.add(AdminResponse.PageItemDTO.createPageItem(i, i + 1, i == page, keyword, sort, selectedCategory));
        }

        return pageItems;
    }

    private List<AdminResponse.AdminBoardDTO> createAdminBoardModels(List<Board> boards) {
        List<AdminResponse.AdminBoardDTO> boardModels = new ArrayList<>();

        for (Board board : boards) {
            boardModels.add(AdminResponse.AdminBoardDTO.createAdminBoard(
                    board.getId(),
                    board.getTitle(),
                    board.getUser().getUsername(),
                    board.getCreatedAt().toLocalDate(),
                    board.getViewCount(),
                    toCategoryLabel(board.getCategory()),
                    toCategoryClass(board.getCategory())));
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
                "관리 필요 계정", "person_off", "metric-icon-rose"));
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
        items.add(createCategoryChartItem("여행 팁", boardQueryRepository.countByCategory("tips"), totalBoardCount,
                "admin-category-badge cat-tips", "admin-chart-bar--tips"));
        items.add(createCategoryChartItem("여행 계획", boardQueryRepository.countByCategory("plan"), totalBoardCount,
                "admin-category-badge cat-plan", "admin-chart-bar--plan"));
        items.add(createCategoryChartItem("맛집/카페", boardQueryRepository.countByCategory("food"), totalBoardCount,
                "admin-category-badge cat-food", "admin-chart-bar--food"));
        items.add(createCategoryChartItem("숙소 후기", boardQueryRepository.countByCategory("review"), totalBoardCount,
                "admin-category-badge cat-review", "admin-chart-bar--review"));
        items.add(createCategoryChartItem("질문/답변", boardQueryRepository.countByCategory("qna"), totalBoardCount,
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
        List<User> users = adminUserQueryRepository.findRecentUsers(DASHBOARD_RECENT_LIMIT);
        List<AdminResponse.RecentUserDTO> recentUsers = new ArrayList<>();

        for (User user : users) {
            recentUsers.add(AdminResponse.RecentUserDTO.createRecentUser(
                    user.getUsername(),
                    user.isActive() ? "활성" : "비활성",
                    user.isActive() ? "status-active" : "status-danger",
                    formatDateTime(user.getCreatedAt())));
        }

        return recentUsers;
    }

    private List<AdminResponse.RecentBoardDTO> loadRecentBoards() {
        List<Board> boards = boardQueryRepository.findRecentBoards(DASHBOARD_RECENT_LIMIT);
        List<AdminResponse.RecentBoardDTO> recentBoards = new ArrayList<>();

        for (Board board : boards) {
            recentBoards.add(AdminResponse.RecentBoardDTO.createRecentBoard(
                    board.getId(),
                    board.getTitle(),
                    board.getUser().getUsername(),
                    formatDateTime(board.getCreatedAt())));
        }

        return recentBoards;
    }

    private void requireAdminSessionUser(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        if (!sessionUser.isAdmin()) {
            throw new Exception403("관리자만 제어할 수 있습니다.");
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
        if (category == null || category.isBlank()) {
            return "all";
        }
        return category;
    }

    private boolean isAllCategory(String category) {
        return "all".equals(category);
    }

    private String[] splitKeywords(String keyword) {
        return keyword.split("\\s+");
    }

    private String toSortFieldLabel(String sort) {
        return switch (sort) {
            case "view", "downview" -> "조회순";
            case "likes", "downlikes" -> "좋아요순";
            default -> "날짜순";
        };
    }

    private String toSortDirectionLabel(String sort) {
        return switch (sort) {
            case "downlikes", "downview", "date" -> "↓";
            default -> "↑";
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

    private String toCategoryLabel(String category) {
        if (category == null || category.isBlank()) {
            return "기타";
        }

        return switch (category) {
            case "tips" -> "여행 팁";
            case "plan" -> "여행 계획";
            case "food" -> "맛집/카페";
            case "review" -> "숙소 후기";
            case "qna" -> "질문/답변";
            default -> "기타";
        };
    }

    private String toCategoryClass(String category) {
        if (category == null || category.isBlank()) {
            return "cat-plan";
        }

        return switch (category) {
            case "tips" -> "cat-tips";
            case "plan" -> "cat-plan";
            case "food" -> "cat-food";
            case "review" -> "cat-review";
            case "qna" -> "cat-qna";
            default -> "cat-plan";
        };
    }
}
