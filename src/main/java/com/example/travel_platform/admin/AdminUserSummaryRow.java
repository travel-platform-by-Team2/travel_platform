package com.example.travel_platform.admin;

import java.time.LocalDateTime;

public record AdminUserSummaryRow(
        Integer userId,
        String username,
        String email,
        LocalDateTime createdAt,
        boolean active,
        long boardCount) {
}
