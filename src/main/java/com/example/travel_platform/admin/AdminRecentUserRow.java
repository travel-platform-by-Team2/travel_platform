package com.example.travel_platform.admin;

import java.time.LocalDateTime;

public record AdminRecentUserRow(
        String username,
        boolean active,
        LocalDateTime createdAt) {
}
