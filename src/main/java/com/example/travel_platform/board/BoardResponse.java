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

        public static SummaryDTO from(Board board, int likeCount) {
            String plainText = Jsoup.parse(board.getContent()).text();
            String summary = plainText.substring(0, Math.min(80, plainText.length()));
            LocalDateTime createdAt = board.getCreatedAt();

            return SummaryDTO.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .username(board.getUser().getUsername())
                    .category(board.getCategory())
                    .categoryLabel(toCategoryLabel(board.getCategory()))
                    .categoryClass(toCategoryClass(board.getCategory()))
                    .viewCount(board.getViewCount())
                    .likeCount(likeCount)
                    .replyCount(board.getReplies().size())
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
    }

    @Data
    @Builder
    public static class PageItemDTO {
        private int page;
        private int displayNumber;
        private boolean current;
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

        public static ReplyItemDTO from(Reply reply, Integer sessionUserId) {
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
        private Long likeCount;
        private Boolean likedByMe;

        public static DetailDTO of(Board board,
                List<ReplyItemDTO> replies,
                long likeCount,
                boolean likedByMe,
                boolean isOwner,
                boolean isAdmin) {
            LocalDateTime createdAt = board.getCreatedAt();

            return DetailDTO.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .category(board.getCategory())
                    .categoryLabel(toCategoryLabel(board.getCategory()))
                    .categoryClass(toCategoryClass(board.getCategory()))
                    .username(board.getUser().getUsername())
                    .viewCount(board.getViewCount())
                    .replyCount(replies.size())
                    .createdAt(createdAt)
                    .createdAtDisplay(formatDateTime(createdAt))
                    .replies(replies)
                    .isOwner(isOwner)
                    .isAdmin(isAdmin)
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
            return FormDTO.builder()
                    .category("")
                    .title("")
                    .content("")
                    .build();
        }

        public static FormDTO fromCreate(BoardRequest.CreateDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return FormDTO.builder()
                    .category(reqDTO.getCategory())
                    .title(reqDTO.getTitle())
                    .content(reqDTO.getContent())
                    .categoryError(categoryError)
                    .titleError(titleError)
                    .contentError(contentError)
                    .build();
        }

        public static FormDTO fromBoard(Board board) {
            return FormDTO.builder()
                    .id(board.getId())
                    .category(board.getCategory())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .build();
        }

        public static FormDTO fromUpdate(Integer boardId,
                BoardRequest.UpdateDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return FormDTO.builder()
                    .id(boardId)
                    .category(reqDTO.getCategory())
                    .title(reqDTO.getTitle())
                    .content(reqDTO.getContent())
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

        public static LikeToggleDTO of(boolean liked, long likeCount) {
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

    private static String toCategoryLabel(String category) {
        if (category == null) {
            return "";
        }

        return switch (category) {
            case "tips" -> "여행 팁";
            case "plan" -> "여행 계획";
            case "food" -> "맛집/카페";
            case "review" -> "숙소 후기";
            case "qna" -> "질문/답변";
            default -> category;
        };
    }

    private static String toCategoryClass(String category) {
        if (category == null) {
            return "";
        }

        return switch (category) {
            case "tips" -> "cat-tips";
            case "plan" -> "cat-plan";
            case "food" -> "cat-food";
            case "review" -> "cat-review";
            case "qna" -> "cat-qna";
            default -> "";
        };
    }
}
