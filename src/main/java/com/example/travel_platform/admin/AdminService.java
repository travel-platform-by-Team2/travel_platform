package com.example.travel_platform.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {
    private final BoardRepository boardRepository;

    public AdminResponse.AdminBoardListDTO getBoardList(String category, String keyword, int page) {
        int size = 10;
        int offset = page * size;

        List<Board> boards;
        long categoryCount;

        long allCount = boardRepository.count();

        boolean hasCategory = category != null && !category.isBlank();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword) {
            String[] words = keyword.trim().split("\\s+");
            boards = boardRepository.search(category, words, offset, size);
            categoryCount = boardRepository.countSearch(category, words);
        } else if (hasCategory) {
            boards = boardRepository.findAllPagingByCategory(category, offset, size);
            categoryCount = boardRepository.countByCategory(category);
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

        return AdminResponse.AdminBoardListDTO.builder()
                .boards(boardDTOs)
                .pageItems(pageItems)
                .currentPage(page)
                .totalCount(categoryCount)
                .allCount(allCount)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .category(category)
                .keyword(keyword)
                .selectCategory(category)
                .isTips(isCategory(category, "tips"))
                .isPlan(isCategory(category, "plan"))
                .isFood(isCategory(category, "food"))
                .isReview(isCategory(category, "review"))
                .isQna(isCategory(category, "qna"))
                .build();
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

    // css용
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
