package com.example.travel_platform.admin;

import java.time.LocalDateTime;

import com.example.travel_platform.board.BoardCategory;

public record AdminBoardSummaryRow(
        Integer boardId,
        String title,
        String userName,
        LocalDateTime createdAt,
        Integer viewCount,
        BoardCategory category) {
}
