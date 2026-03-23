package com.example.travel_platform.admin;

import java.time.LocalDateTime;

public record AdminRecentBoardRow(
        Integer boardId,
        String title,
        String userName,
        LocalDateTime createdAt) {
}
