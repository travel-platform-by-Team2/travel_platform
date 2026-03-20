package com.example.travel_platform.admin;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.User;

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

    private final AdminRepository adminRepository;
    private final BoardRepository boardRepository;

    public AdminResponse.DashboardPageDTO getDashboardPage() {
        long totalUserCount = adminRepository.count();
        long inactiveUserCount = adminRepository.countByActiveFalse();
        long activeUserCount = totalUserCount - inactiveUserCount;

        long totalBoardCount = boardRepository.count();
        long recentBoardCount = boardRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(RECENT_BOARD_DAYS));
        long totalBoardViewCount = boardRepository.sumViewCount();

        List<AdminResponse.DashboardMetricDTO> metrics = createDashboardMetrics(
                totalUserCount,
                activeUserCount,
                inactiveUserCount,
                totalBoardCount,
                recentBoardCount,
                totalBoardViewCount);

        List<AdminResponse.StatusChartItemDTO> userStatusItems = createUserStatusItems(
                totalUserCount,
                activeUserCount,
                inactiveUserCount);
        List<AdminResponse.CategoryChartItemDTO> boardCategoryItems = createBoardCategoryItems(totalBoardCount);
        List<AdminResponse.RecentUserDTO> recentUsers = loadRecentUsers();
        List<AdminResponse.RecentBoardDTO> recentBoards = loadRecentBoards();

        return AdminResponse.DashboardPageDTO.of(
                totalUserCount,
                activeUserCount,
                inactiveUserCount,
                totalBoardCount,
                recentBoardCount,
                totalBoardViewCount,
                metrics,
                userStatusItems,
                boardCategoryItems,
                recentUsers,
                recentBoards);
    }

    public AdminResponse.UserListPageDTO getUsersPage(Boolean active, String keyword, String sortBy, String orderBy) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSortBy = normalizeUserSortBy(sortBy);
        String normalizedOrderBy = normalizeUserOrderBy(orderBy);
        List<User> users = findUsers(active, normalizedKeyword);
        return createUsersPage(users, normalizedKeyword, active, normalizedSortBy, normalizedOrderBy);
    }

    @Transactional
    public void updateUserActive(Integer userId, boolean active) {
        User user = findUser(userId);
        user.setActive(active);
    }

    @Transactional
    public void deleteBoard(SessionUser sessionUser, Integer boardId) {
        requireAdmin(sessionUser);
        Board board = findBoard(boardId);
        deleteBoardRelations(boardId);
        boardRepository.delete(board);
    }

    public AdminResponse.AdminBoardListDTO getBoardsPage(String category, String keyword, String sort, int page) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSort = normalizeSort(sort);
        String allCategory = resolveAllCategory(category);
        boolean allCategoryTab = isAllCategory(allCategory);
        List<Board> boards = findBoards(allCategory, normalizedKeyword, normalizedSort, page);
        long totalCount = countBoards(allCategory, normalizedKeyword);
        return createBoardsPage(category, normalizedKeyword, normalizedSort, allCategory, allCategoryTab, boards, totalCount,
                page);
    }

    private List<User> findUsers(Boolean active, String keyword) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword && active != null) {
            return adminRepository.findByActiveAndKeyword(active, keyword);
        }
        if (hasKeyword) {
            return adminRepository.findByKeyword(keyword);
        }
        if (active == null) {
            return adminRepository.findAllByOrderByCreatedAtDescIdDesc();
        }
        return adminRepository.findByActiveOrderByCreatedAtDescIdDesc(active);
    }

    private AdminResponse.UserListPageDTO createUsersPage(
            List<User> users,
            String normalizedKeyword,
            Boolean active,
            String normalizedSortBy,
            String normalizedOrderBy) {
        List<AdminResponse.AdminUserDTO> userDTOs = createAdminUserDTOs(users, normalizedSortBy, normalizedOrderBy);

        return AdminResponse.UserListPageDTO.of(
                userDTOs,
                adminRepository.count(),
                adminRepository.countByActiveFalse(),
                normalizedKeyword,
                active,
                active == null,
                Boolean.TRUE.equals(active),
                Boolean.FALSE.equals(active),
                normalizedSortBy,
                normalizedOrderBy);
    }

    private void requireAdmin(SessionUser sessionUser) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        if (!sessionUser.isAdmin()) {
            throw new Exception403("관리자만 제어할 수 있습니다.");
        }
    }

    private Board findBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
    }

    private void deleteBoardRelations(Integer boardId) {
        boardRepository.deleteLikesByBoard(boardId);
    }

    private List<Board> findBoards(String allCategory, String normalizedKeyword, String sort, int page) {
        int offset = page * BOARD_PAGE_SIZE;
        boolean hasKeyword = !normalizedKeyword.isBlank();

        if (hasKeyword) {
            String[] words = splitKeywords(normalizedKeyword);

            if (isAllCategory(allCategory)) {
                return boardRepository.search(null, words, sort, offset, BOARD_PAGE_SIZE);
            }
            return boardRepository.search(allCategory, words, sort, offset, BOARD_PAGE_SIZE);
        }

        if (isAllCategory(allCategory)) {
            return boardRepository.findAllPaging(sort, offset, BOARD_PAGE_SIZE);
        }

        return boardRepository.findAllPagingByCategory(allCategory, sort, offset, BOARD_PAGE_SIZE);
    }

    private long countBoards(String allCategory, String normalizedKeyword) {
        boolean hasKeyword = !normalizedKeyword.isBlank();

        if (hasKeyword) {
            String[] words = splitKeywords(normalizedKeyword);

            if (isAllCategory(allCategory)) {
                return boardRepository.countSearch(null, words);
            }
            return boardRepository.countSearch(allCategory, words);
        }

        if (isAllCategory(allCategory)) {
            return boardRepository.count();
        }

        return boardRepository.countByCategory(allCategory);
    }

    private AdminResponse.AdminBoardListDTO createBoardsPage(
            String category,
            String normalizedKeyword,
            String normalizedSort,
            String allCategory,
            boolean allCategoryTab,
            List<Board> boards,
            long totalCount,
            int page) {
        int totalPages = getTotalPages(totalCount);
        List<AdminResponse.PageItemDTO> pageItems = createBoardPageItems(
                page,
                totalPages,
                normalizedKeyword,
                normalizedSort,
                allCategory);
        List<AdminResponse.AdminBoardDTO> boardDTOs = createBoardDTOs(boards);

        return AdminResponse.AdminBoardListDTO.of(
                boardDTOs,
                pageItems,
                page,
                totalPages,
                totalCount,
                boardRepository.count(),
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
                allCategory,
                allCategoryTab,
                allCategoryTab ? null : allCategory,
                isCategory(category, "tips"),
                isCategory(category, "plan"),
                isCategory(category, "food"),
                isCategory(category, "review"),
                isCategory(category, "qna"));
    }

    private List<AdminResponse.AdminUserDTO> createAdminUserDTOs(List<User> users, String sortBy, String orderBy) {
        Map<Integer, Long> boardCounts = boardRepository.countBoardsByUserIds(
                users.stream().map(User::getId).toList());

        List<AdminResponse.AdminUserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(AdminResponse.AdminUserDTO.of(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getCreatedAt().toLocalDate(),
                    user.isActive(),
                    Math.toIntExact(boardCounts.getOrDefault(user.getId(), 0L)),
                    user.isActive() ? "활성" : "비활성",
                    user.isActive() ? "비활성" : "활성"));
        }

        userDTOs.sort(buildUserSortComparator(sortBy, orderBy));
        return userDTOs;
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

    private List<AdminResponse.PageItemDTO> createBoardPageItems(
            int page,
            int totalPages,
            String keyword,
            String sort,
            String allCategory) {
        int startPage = (page / BOARD_PAGE_BLOCK_SIZE) * BOARD_PAGE_BLOCK_SIZE;
        int endPage = Math.min(startPage + BOARD_PAGE_BLOCK_SIZE - 1, totalPages - 1);

        List<AdminResponse.PageItemDTO> pageItems = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageItems.add(AdminResponse.PageItemDTO.of(i, i + 1, i == page, keyword, sort, allCategory));
        }
        return pageItems;
    }

    private List<AdminResponse.AdminBoardDTO> createBoardDTOs(List<Board> boards) {
        List<AdminResponse.AdminBoardDTO> boardDTOs = new ArrayList<>();
        for (Board board : boards) {
            boardDTOs.add(AdminResponse.AdminBoardDTO.of(
                    board.getId(),
                    board.getTitle(),
                    board.getUser().getUsername(),
                    board.getCreatedAt().toLocalDate(),
                    board.getViewCount(),
                    toCategoryLabel(board.getCategory()),
                    toCategoryClass(board.getCategory())));
        }
        return boardDTOs;
    }

    private String resolveAllCategory(String category) {
        if (category == null || category.isBlank()) {
            return "all";
        }
        return category;
    }

    private boolean isAllCategory(String allCategory) {
        return "all".equals(allCategory);
    }

    private String[] splitKeywords(String normalizedKeyword) {
        return normalizedKeyword.split("\\s+");
    }

    private int getTotalPages(long totalCount) {
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
        metrics.add(AdminResponse.DashboardMetricDTO.of("전체 사용자", formatCount(totalUserCount),
                "활성 " + formatCount(activeUserCount) + "명", "groups", "metric-icon-blue"));
        metrics.add(AdminResponse.DashboardMetricDTO.of("비활성 사용자", formatCount(inactiveUserCount), "관리 필요 계정",
                "person_off", "metric-icon-rose"));
        metrics.add(AdminResponse.DashboardMetricDTO.of("전체 게시글", formatCount(totalBoardCount),
                "최근 7일 " + formatCount(recentBoardCount) + "개", "forum", "metric-icon-purple"));
        metrics.add(AdminResponse.DashboardMetricDTO.of("누적 조회수", formatCount(totalBoardViewCount), "커뮤니티 전체 기준",
                "visibility", "metric-icon-orange"));
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
        return AdminResponse.StatusChartItemDTO.of(label, formatCount(count) + "명", percent + "%", percent, badgeClass,
                barClass);
    }

    private List<AdminResponse.CategoryChartItemDTO> createBoardCategoryItems(long totalBoardCount) {
        List<AdminResponse.CategoryChartItemDTO> items = new ArrayList<>();
        items.add(createCategoryChartItem("여행 팁", boardRepository.countByCategory("tips"), totalBoardCount,
                "admin-category-badge cat-tips", "admin-chart-bar--tips"));
        items.add(createCategoryChartItem("여행 계획", boardRepository.countByCategory("plan"), totalBoardCount,
                "admin-category-badge cat-plan", "admin-chart-bar--plan"));
        items.add(createCategoryChartItem("맛집/카페", boardRepository.countByCategory("food"), totalBoardCount,
                "admin-category-badge cat-food", "admin-chart-bar--food"));
        items.add(createCategoryChartItem("숙소 후기", boardRepository.countByCategory("review"), totalBoardCount,
                "admin-category-badge cat-review", "admin-chart-bar--review"));
        items.add(createCategoryChartItem("질문/답변", boardRepository.countByCategory("qna"), totalBoardCount,
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
        return AdminResponse.CategoryChartItemDTO.of(label, formatCount(count) + "개", percent + "%", percent,
                badgeClass, barClass);
    }

    private List<AdminResponse.RecentUserDTO> loadRecentUsers() {
        List<User> users = adminRepository.findRecentUsers(PageRequest.of(0, DASHBOARD_RECENT_LIMIT));
        List<AdminResponse.RecentUserDTO> recentUsers = new ArrayList<>();
        for (User user : users) {
            recentUsers.add(AdminResponse.RecentUserDTO.of(
                    user.getUsername(),
                    user.isActive() ? "활성" : "비활성",
                    user.isActive() ? "status-active" : "status-danger",
                    formatDateTime(user.getCreatedAt())));
        }
        return recentUsers;
    }

    private List<AdminResponse.RecentBoardDTO> loadRecentBoards() {
        List<Board> boards = boardRepository.findRecentBoards(DASHBOARD_RECENT_LIMIT);
        List<AdminResponse.RecentBoardDTO> recentBoards = new ArrayList<>();
        for (Board board : boards) {
            recentBoards.add(AdminResponse.RecentBoardDTO.of(
                    board.getId(),
                    board.getTitle(),
                    board.getUser().getUsername(),
                    formatDateTime(board.getCreatedAt())));
        }
        return recentBoards;
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

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "latest";
        }
        return switch (sort) {
            case "likes", "downlikes", "view", "downview", "latest", "date" -> sort;
            default -> "latest";
        };
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
        boolean isAsc = isAscendingSort(sort);

        return switch (field) {
            case "date" -> isAsc ? "date" : "latest";
            case "view" -> isAsc ? "downview" : "view";
            case "likes" -> isAsc ? "downlikes" : "likes";
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

    private User findUser(Integer userId) {
        return adminRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
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
