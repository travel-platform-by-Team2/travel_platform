package com.example.travel_platform.board.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ReplyRequest {

    @Data
    public static class CreateDTO {
        @NotBlank(message = "댓글을 입력해주세요.")
        private String content;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }
}
