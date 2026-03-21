package com.example.travel_platform.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class BoardRequest {

    private static final String CATEGORY_PATTERN = "tips|plan|food|review|qna";

    @Data
    public static class CreateDTO {
        @NotBlank(message = "카테고리를 선택해주세요.")
        @Pattern(regexp = CATEGORY_PATTERN, message = "유효한 카테고리를 선택해주세요.")
        private String category;

        @NotBlank(message = "제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank(message = "카테고리를 선택해주세요.")
        @Pattern(regexp = CATEGORY_PATTERN, message = "유효한 카테고리를 선택해주세요.")
        private String category;

        @NotBlank(message = "제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }
}
