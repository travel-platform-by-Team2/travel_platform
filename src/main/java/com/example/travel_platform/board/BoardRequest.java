package com.example.travel_platform.board;

import com.example.travel_platform._core.validation.ValidEnumCode;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class BoardRequest {

    @Data
    public static class CreateDTO {
        @NotBlank(message = "카테고리를 선택해주세요.")
        @ValidEnumCode(enumClass = BoardCategory.class, message = "유효한 카테고리를 선택해주세요.")
        private String category;

        @NotBlank(message = "제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank(message = "카테고리를 선택해주세요.")
        @ValidEnumCode(enumClass = BoardCategory.class, message = "유효한 카테고리를 선택해주세요.")
        private String category;

        @NotBlank(message = "제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }
}
