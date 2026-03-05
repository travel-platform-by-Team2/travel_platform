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
        private Integer viewCount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class ReplyDTO {
        private Integer id;
        private String username;
        private String content;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class BoardDetailDTO {
        private Integer id;
        private String title;
        private String content;
        private String username;
        private Integer viewCount;
        private LocalDateTime createdAt;
        private List<ReplyDTO> replies;
    }
}
