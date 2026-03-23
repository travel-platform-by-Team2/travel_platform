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

    @Data
    @Builder
    public static class SummaryDTO {
        private Integer id;
        private String title;
        private String username;
        private String category;
        private String categoryLabel;
        private String categoryClass;
        private Integer viewCount;
        private Integer likeCount;
        private Integer replyCount;
        private LocalDateTime createdAt;
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
                    .category(category.getCode())
                    .categoryLabel(category.getLabel())
                    .categoryClass(category.getCssClass())
                    .viewCount(board.getViewCount())
                    .likeCount(likeCount)
                    .replyCount(replyCount)
                    .createdAt(createdAt)
                    .createdAtDisplay(formatDateTime(createdAt))
                    .summary(summary)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ListPageDTO {
        private List<SummaryDTO> boards;
        private List<PageItemDTO> pageItems;
        private int currentPage;
        private int pageNumber;
        private int size;
        private long totalCount;
        private int totalPages;
        private boolean first;
        private boolean last;
        private Integer prevPage;
        private Integer nextPage;
        private String category;
        private String keyword;
        private String sort;
        private String sortLabel;
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

        public static ListPageDTO createListPage(List<SummaryDTO> boards,
                List<PageItemDTO> pageItems,
                int currentPage,
                int size,
                long totalCount,
                int totalPages,
                String category,
                String keyword,
                String sort) {
            boolean first = currentPage == 0;
            boolean last = currentPage >= totalPages - 1;

            return ListPageDTO.builder()
                    .boards(boards)
                    .pageItems(pageItems)
                    .currentPage(currentPage)
                    .pageNumber(currentPage + 1)
                    .size(size)
                    .totalCount(totalCount)
                    .totalPages(totalPages)
                    .first(first)
                    .last(last)
                    .prevPage(first ? null : currentPage - 1)
                    .nextPage(last ? null : currentPage + 1)
                    .category(category)
                    .keyword(keyword)
                    .sort(sort)
                    .sortLabel(toSortLabel(sort))
                    .isSortLikes(isSort(sort, "likes"))
                    .isSortDownlikes(isSort(sort, "downlikes"))
                    .isSortViews(isSort(sort, "view"))
                    .isSortDownviews(isSort(sort, "downview"))
                    .isSortLatest(isSort(sort, "latest"))
                    .isSortDate(isSort(sort, "date"))
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
        private LocalDateTime createdAt;
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
                    .createdAt(createdAt)
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
        private String category;
        private String categoryLabel;
        private String categoryClass;
        private String username;
        private Integer viewCount;
        private Integer replyCount;
        private LocalDateTime createdAt;
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
                    .category(category.getCode())
                    .categoryLabel(category.getLabel())
                    .categoryClass(category.getCssClass())
                    .username(board.getUser().getUsername())
                    .viewCount(board.getViewCount())
                    .replyCount(replies.size())
                    .createdAt(createdAt)
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

    private static String toSortLabel(String sort) {
        return switch (sort) {
            case "likes" -> "좋아요 많은 순";
            case "downlikes" -> "좋아요 적은 순";
            case "view" -> "조회수 많은 순";
            case "downview" -> "조회수 적은 순";
            case "date" -> "날짜 오래된 순";
            default -> "날짜 최신 순";
        };
    }

    private static boolean isSort(String actualSort, String expectedSort) {
        return expectedSort.equals(actualSort);
    }

    private static boolean isCategory(String actualCategory, String expectedCategory) {
        return expectedCategory.equals(actualCategory);
    }
}
