package com.example.travel_platform.board.reply;

import lombok.Data;

public class ReplyRequest {

    @Data
    public static class CreateDTO {
        private String content;
    }

    @Data
    public static class UpdateDTO {
        private String content;
    }
}
