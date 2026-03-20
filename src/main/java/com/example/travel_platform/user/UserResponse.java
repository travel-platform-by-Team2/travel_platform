package com.example.travel_platform.user;

import java.time.LocalDateTime;

import lombok.Data;

public class UserResponse {

    @Data
    public static class AdminListDTO {
        private Integer userId;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private boolean active;
        private int boardCount;
        private String statusText;

        public static AdminListDTO fromUser(User user) {
            return fromUser(user, 0);
        }

        public static AdminListDTO fromUser(User user, int boardCount) {
            AdminListDTO dto = new AdminListDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setActive(user.isActive());
            dto.setBoardCount(boardCount);
            dto.setStatusText(user.isActive() ? "활성" : "비활성");
            return dto;
        }
    }
}
