package com.example.travel_platform.board;

import java.time.LocalDateTime;
import java.util.List;

import com.example.travel_platform.board.reply.Reply;

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
        private Integer viewCount;
        private Integer replyCount;
        private LocalDateTime createdAt;
        private String summary;
    }

    @Data
    @Builder
    public static class ReplyDTO {
        private Integer id;
        private Integer boardId;
        private String username;
        private String content;
        private LocalDateTime createdAt;
        private boolean isOwner;
    }

    private ReplyDTO toReplyDTO(Reply reply, Integer sessionUserId) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .boardId(reply.getBoard().getId())
                .username(reply.getUser().getUsername())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .isOwner(reply.getUser().getId().equals(sessionUserId))
                .build();
    }

    @Data
    @Builder
    public static class BoardDetailDTO {
        private Integer id;
        private String title;
        private String content;
        private String username;
        private Integer viewCount;
        private Integer replyCount;
        private LocalDateTime createdAt;
        private List<ReplyDTO> replies;

        private Boolean isOwner;
        private String titleError;
        private String contentError;
    }
}
