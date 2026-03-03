package com.example.travel_platform.community;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 커뮤니티 게시글/댓글 관련 요청 바디를 정의하는 DTO 클래스
public class CommunityRequest {

    // 커뮤니티 게시글 생성 요청 DTO
    @Data
    public static class CreatePostDTO {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }

    // 커뮤니티 게시글 수정 요청 DTO
    @Data
    public static class UpdatePostDTO {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }

    // 커뮤니티 댓글 생성 요청 DTO
    @Data
    public static class CreateReplyDTO {
        @NotBlank
        private String content;
    }
}
