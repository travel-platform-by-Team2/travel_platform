package com.example.travel_platform.board;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jsoup.Jsoup;

import com.example.travel_platform.board.reply.Reply;

import lombok.Builder;
import lombok.Data;

public class BoardResponse {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static String sortClass(BoardSort currentSort, BoardSort targetSort) {
        if (currentSort == targetSort) {
            return " is-active";
        }
        return "";
    }

    @Data
    @Builder
    public static class SummaryDTO {
        private Integer id;
        private String title;
        private String username;
        private String categoryLabel;
        private String categoryClass;
        private Integer viewCount;
        private Integer likeCount;
        private Integer replyCount;
        private String createdAtDisplay;
        private String summary;

        public static SummaryDTO fromBoard(Board board, int likeCount, int replyCount) {
            String plainText = Jsoup.parse(board.getContent()).text();
            String summary = plainText.substring(0, Math.min(80, plainText.length()));
            LocalDateTime createdAt = board.getCreatedAt();
            BoardCategory category = board.getCategory();

            return SummaryDTO.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .username(board.getUser().getUsername())
                    .categoryLabel(category.getLabel())
                    .categoryClass(category.getCssClass())
                    .viewCount(board.getViewCount())
                    .likeCount(likeCount)
                    .replyCount(replyCount)
                    .createdAtDisplay(formatDateTime(createdAt))
                    .summary(summary)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ListPageDTO {
        private List<PageItemDTO> pageItems;
        private int currentPage;
        private int totalPages;
        private Integer prevPage;
        private Integer nextPage;
        private String category;
        private String keyword;
        private String sort;
        private String sortLabel;
        private String likeClass;
        private String lowLikeClass;
        private String viewClass;
        private String lowViewClass;
        private String latestClass;
        private String dateClass;
        private boolean isTips;
        private boolean isPlan;
        private boolean isFood;
        private boolean isReview;
        private boolean isQna;

        public static ListPageDTO createListPage(List<PageItemDTO> pageItems,
                int currentPage,
                int totalPages,
                String category,
                String keyword,
                BoardSort sort) {
            boolean first = currentPage == 0;
            boolean last = currentPage >= totalPages - 1;

            return ListPageDTO.builder()
                    .pageItems(pageItems)
                    .currentPage(currentPage)
                    .totalPages(totalPages)
                    .prevPage(first ? null : currentPage - 1)
                    .nextPage(last ? null : currentPage + 1)
                    .category(category)
                    .keyword(keyword)
                    .sort(sort.getCode())
                    .sortLabel(sort.getLabel())
                    .likeClass(sortClass(sort, BoardSort.LIKES))
                    .lowLikeClass(sortClass(sort, BoardSort.DOWNLIKES))
                    .viewClass(sortClass(sort, BoardSort.VIEW))
                    .lowViewClass(sortClass(sort, BoardSort.DOWNVIEW))
                    .latestClass(sortClass(sort, BoardSort.LATEST))
                    .dateClass(sortClass(sort, BoardSort.DATE))
                    .isTips(isCategory(category, "tips"))
                    .isPlan(isCategory(category, "plan"))
                    .isFood(isCategory(category, "food"))
                    .isReview(isCategory(category, "review"))
                    .isQna(isCategory(category, "qna"))
                    .build();
        }
    }

    @Data
    @Builder
    public static class ListViewDTO {
        private ListPageDTO model;
        private List<SummaryDTO> models;

        public static ListViewDTO createListView(ListPageDTO model, List<SummaryDTO> models) {
            return ListViewDTO.builder()
                    .model(model)
                    .models(models)
                    .build();
        }
    }

    @Data
    @Builder
    public static class PageItemDTO {
        private int page;
        private int displayNumber;
        private boolean current;

        public static PageItemDTO createPageItem(int page, boolean current) {
            return PageItemDTO.builder()
                    .page(page)
                    .displayNumber(page + 1)
                    .current(current)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ReplyItemDTO {
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private String createdAtDisplay;
        private boolean isOwner;

        public static ReplyItemDTO fromReply(Reply reply, Integer sessionUserId) {
            boolean isOwner = sessionUserId != null && reply.getUser().getId().equals(sessionUserId);
            LocalDateTime createdAt = reply.getCreatedAt();

            return ReplyItemDTO.builder()
                    .id(reply.getId())
                    .boardId(reply.getBoard().getId())
                    .username(reply.getUser().getUsername())
                    .content(reply.getContent())
                    .createdAtDisplay(formatDateTime(createdAt))
                    .isOwner(isOwner)
                    .build();
        }
    }

    @Data
    @Builder
    public static class DetailDTO {
        private Integer id;
        private String title;
        private String content;
        private String categoryLabel;
        private String categoryClass;
        private String username;
        private Integer viewCount;
        private Integer replyCount;
        private String createdAtDisplay;
        private List<ReplyItemDTO> replies;
        private Boolean isOwner;
        private Boolean isAdmin;
        private Boolean canManage;
        private Long likeCount;
        private Boolean likedByMe;

        public static DetailDTO fromBoard(Board board,
                List<ReplyItemDTO> replies,
                long likeCount,
                boolean likedByMe,
                boolean isOwner,
                boolean isAdmin) {
            LocalDateTime createdAt = board.getCreatedAt();
            BoardCategory category = board.getCategory();

            return DetailDTO.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .categoryLabel(category.getLabel())
                    .categoryClass(category.getCssClass())
                    .username(board.getUser().getUsername())
                    .viewCount(board.getViewCount())
                    .replyCount(replies.size())
                    .createdAtDisplay(formatDateTime(createdAt))
                    .replies(replies)
                    .isOwner(isOwner)
                    .isAdmin(isAdmin)
                    .canManage(isOwner || isAdmin)
                    .likeCount(likeCount)
                    .likedByMe(likedByMe)
                    .build();
        }
    }

    @Data
    @Builder
    public static class FormDTO {
        private Integer id;
        private String category;
        private String title;
        private String content;
        private String categoryError;
        private String titleError;
        private String contentError;

        public static FormDTO empty() {
            return createForm(null, "", "", "", null, null, null);
        }

        public static FormDTO fromCreateRequest(BoardRequest.CreateDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return createForm(
                    null,
                    reqDTO.getCategory(),
                    reqDTO.getTitle(),
                    reqDTO.getContent(),
                    categoryError,
                    titleError,
                    contentError);
        }

        public static FormDTO fromBoard(Board board) {
            return createForm(
                    board.getId(),
                    board.getCategoryCode(),
                    board.getTitle(),
                    board.getContent(),
                    null,
                    null,
                    null);
        }

        public static FormDTO fromUpdateRequest(Integer boardId,
                BoardRequest.UpdateDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return createForm(
                    boardId,
                    reqDTO.getCategory(),
                    reqDTO.getTitle(),
                    reqDTO.getContent(),
                    categoryError,
                    titleError,
                    contentError);
        }

        private static FormDTO createForm(Integer id,
                String category,
                String title,
                String content,
                String categoryError,
                String titleError,
                String contentError) {
            return FormDTO.builder()
                    .id(id)
                    .category(category)
                    .title(title)
                    .content(content)
                    .categoryError(categoryError)
                    .titleError(titleError)
                    .contentError(contentError)
                    .build();
        }
    }

    @Data
    @Builder
    public static class LikeToggleDTO {
        private boolean liked;
        private long likeCount;

        public static LikeToggleDTO createLikeToggle(boolean liked, long likeCount) {
            return LikeToggleDTO.builder()
                    .liked(liked)
                    .likeCount(likeCount)
                    .build();
        }
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private static boolean isCategory(String actualCategory, String expectedCategory) {
        return expectedCategory.equals(actualCategory);
    }
}
