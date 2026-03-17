package com.example.travel_platform.board;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

// 게시글/댓글 조회 응답 포맷을 정의하는 DTO 클래스
public class BoardResponse {

    @Data
    @Builder
    public static class BoardSummaryDTO {
        private Integer id;
        private String title;
        private String username;
        private String category;
        private String categoryLabel;
        private String categoryClass;
        private Integer viewCount;
        private Integer replyCount;
        private LocalDateTime createdAt;
        private String createdAtDisplay;
        private String summary;
    }

    @Data
    @Builder
    public static class BoardListPageDTO {
        private List<BoardSummaryDTO> boards;
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
    public static class ReplyDTO {
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private String createdAtDisplay;
        private boolean isOwner;
    }

    @Data
    @Builder
    public static class BoardDetailDTO {
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
        private List<ReplyDTO> replies;

        private Boolean isOwner;
        private String titleError;
        private String contentError;
        private String categoryError;

        private Long likeCount;
        private Boolean likedByMe;
    }

    @Data
    @Builder
    public static class BoardFormDTO {
        private Integer id;
        private String category;
        private String title;
        private String content;
        private String categoryError;
        private String titleError;
        private String contentError;

        public static BoardFormDTO empty() {
            return BoardFormDTO.builder()
                    .category("")
                    .title("")
                    .content("")
                    .build();
        }

        public static BoardFormDTO fromCreate(BoardRequest.CreateBoardDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return BoardFormDTO.builder()
                    .category(reqDTO.getCategory())
                    .title(reqDTO.getTitle())
                    .content(reqDTO.getContent())
                    .categoryError(categoryError)
                    .titleError(titleError)
                    .contentError(contentError)
                    .build();
        }

        public static BoardFormDTO fromDetail(BoardDetailDTO board) {
            return BoardFormDTO.builder()
                    .id(board.getId())
                    .category(board.getCategory())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .build();
        }

        public static BoardFormDTO fromUpdate(BoardDetailDTO board,
                BoardRequest.UpdateBoardDTO reqDTO,
                String categoryError,
                String titleError,
                String contentError) {
            return BoardFormDTO.builder()
                    .id(board.getId())
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
    public static class ToggleLikeDTO {
        private boolean liked;
        private long likeCount;
    }
}
