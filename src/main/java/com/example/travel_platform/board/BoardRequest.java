package com.example.travel_platform.board;

import lombok.Data;

public class BoardRequest {

    @Data
    public static class CreateBoardDTO {
        private String title;
        private String content;
    }

    @Data
    public static class UpdateBoardDTO {
        private String title;
        private String content;
    }
}
