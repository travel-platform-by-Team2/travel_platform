package com.example.travel_platform.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 게시글/댓글 관련 요청 바디를 정의하는 DTO 클래스
public class BoardRequest {

    @Data
    public static class CreateBoardDTO {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }

    @Data
    public static class UpdateBoardDTO {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }
}
