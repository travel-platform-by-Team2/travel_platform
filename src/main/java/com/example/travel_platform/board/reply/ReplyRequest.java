package com.example.travel_platform.board.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ReplyRequest {

    @Data
    public static class CreateDTO {
        @NotBlank
        private String content;
    }

    @Data
    public static class UpdateDTO {
        @NotBlank
        private String content;
    }
}
