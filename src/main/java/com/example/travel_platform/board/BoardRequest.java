package com.example.travel_platform.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class BoardRequest {

        @Data
        public static class CreateBoardDTO {
                @NotBlank(message = "제목을 입력해주세요.")
                private String title;

                @NotBlank(message = "내용을 입력해주세요.")
                private String content;
        }

        @Data
        public static class UpdateBoardDTO {
                @NotBlank(message = "제목을 입력해주세요.")
                private String title;

                @NotBlank(message = "내용을 입력해주세요.")
                private String content;
        }
}
