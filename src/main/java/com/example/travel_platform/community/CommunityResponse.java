package com.example.travel_platform.community;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

// 커뮤니티 게시글/댓글 조회 응답 포맷을 정의하는 DTO 클래스
public class CommunityResponse {

    // 커뮤니티 게시글 목록 아이템 응답 DTO
    @Data
    @Builder
    public static class PostSummaryDTO {
        private Integer id;
        private String title;
        private String username;
        private Integer viewCount;
        private LocalDateTime createdAt;
    }

    // 커뮤니티 댓글 단건 응답 DTO
    @Data
    @Builder
    public static class ReplyDTO {
        private Integer id;
        private String username;
        private String content;
        private LocalDateTime createdAt;
    }

    // 커뮤니티 게시글 상세 응답 DTO
    @Data
    @Builder
    public static class PostDetailDTO {
        private Integer id;
        private String title;
        private String content;
        private String username;
        private Integer viewCount;
        private LocalDateTime createdAt;
        private List<ReplyDTO> replies;
    }
}
