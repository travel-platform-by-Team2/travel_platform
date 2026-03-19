package com.example.travel_platform.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserResponse;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final BoardRepository boardRepository;

    // 관리자 페이지에서 사용할 유저 목록을 조회
    public List<UserResponse.AdminListDTO> getAdminUserList(Boolean active, String keyword) {
        String searchKeyword = normalizeKeyword(keyword);
        boolean hasKeyword = !searchKeyword.isEmpty();
        List<User> users;

        // 키워드 없음 → 전체 조회
        if (active == null && !hasKeyword) {
            users = adminRepository.findAll();
            // 키워드 있음 → 키워드 조회
        } else if (active == null) {
            users = adminRepository.findByUsernameContainingOrEmailContaining(searchKeyword, searchKeyword);
            // 키워드 없음 → 상태 조회
        } else if (!hasKeyword) {
            users = adminRepository.findByActive(active);
            // 키워드 있음 → 상태 + 키워드 조회
        } else {
            users = adminRepository.findByActiveAndKeyword(active, searchKeyword);
        }

        return users.stream()
                .map(user -> UserResponse.AdminListDTO.fromUser(user))
                .toList();
    }

    public long getTotalUserCount() {
        return adminRepository.count();
    }

    public long getInactiveUserCount() {
        return adminRepository.countByActiveFalse();
    }

    @Transactional
    public void toggleUserActive(Integer userId) {
        User user = findUser(userId);
        user.setActive(!user.isActive());
    }

    @Transactional
    public void deleteBoard(User sessionUser, Integer boardId) {
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }

        if (!sessionUser.isAdmin()) {
            throw new Exception403("관리자만 삭제할 수 있습니다.");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을수가 없습니다"));

        boardRepository.deleteLikesByBoard(boardId);
        boardRepository.delete(board);
    }

    public AdminResponse.AdminBoardListDTO getBoardList(String category, String keyword, int page) {
        int size = 10;
        int offset = page * size;

        String allCategory = (category == null || category.isBlank()) ? "all" : category;

        List<Board> boards;
        long categoryCount;

        long allCount = boardRepository.count();

        boolean isAllCategory = "all".equals(allCategory);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword) {
            String[] words = keyword.trim().split("\\s+");

            if (isAllCategory) {
                boards = boardRepository.search(null, words, offset, size);
                categoryCount = boardRepository.countSearch(null, words);
            } else {
                boards = boardRepository.search(allCategory, words, offset, size);
                categoryCount = boardRepository.countSearch(allCategory, words);
            }
        } else if (!isAllCategory) {
            boards = boardRepository.findAllPagingByCategory(allCategory, offset, size);
            categoryCount = boardRepository.countByCategory(allCategory);
        } else {
            boards = boardRepository.findAllPaging(offset, size);
            categoryCount = boardRepository.count();
        }

        int totalPages = (int) Math.ceil((double) categoryCount / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int blockSize = 5;
        int startPage = (page / blockSize) * blockSize;
        int endPage = startPage + blockSize - 1;

        if (endPage >= totalPages) {
            endPage = totalPages - 1;
        }

        List<AdminResponse.PageItemDTO> pageItems = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            pageItems.add(AdminResponse.PageItemDTO.builder()
                    .page(i)
                    .displayNumber(i + 1)
                    .keyword(keyword)
                    .current(i == page)
                    .build());
        }

        List<AdminResponse.AdminBoardDTO> boardDTOs = boards.stream()
                .map(board -> AdminResponse.AdminBoardDTO.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .userName(board.getUser().getUsername())
                        .startDate(board.getCreatedAt().toLocalDate())
                        .viewCount(board.getViewCount())
                        .category(toCategoryLabel(board.getCategory()))
                        .categoryClass(toCategoryClass(board.getCategory()))
                        .build())
                .toList();

        Integer prevPage = page == 0 ? null : page - 1;
        Integer nextPage = boardDTOs.size() < size ? null : page + 1;

        AdminResponse.AdminBoardListDTO adminListDTO = AdminResponse.AdminBoardListDTO.builder()
                .boards(boardDTOs)
                .pageItems(pageItems)
                .currentPage(page)
                .totalCount(categoryCount)
                .allCount(allCount)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .keyword(keyword)
                .allCategory(allCategory)
                .isTips(isCategory(category, "tips"))
                .isPlan(isCategory(category, "plan"))
                .isFood(isCategory(category, "food"))
                .isReview(isCategory(category, "review"))
                .isQna(isCategory(category, "qna"))
                .build();

        return adminListDTO;
    }

    private boolean isCategory(String category, String targetCategory) {
        return targetCategory.equals(category);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
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
