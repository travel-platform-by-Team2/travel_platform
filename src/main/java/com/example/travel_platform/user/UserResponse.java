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
        private String statusText;
        private String managementLabel;

        // User 객체를 AdminListDTO로 변환하는 메서드
        public static AdminListDTO fromUser(User user) {
            AdminListDTO dto = new AdminListDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setCreatedAt(user.getCreatedAt());
            dto.setActive(user.isActive());
            dto.setStatusText(user.isActive() ? "활성" : "비활성");
            dto.setManagementLabel(user.isActive() ? "비활성화" : "활성화");
            return dto;
        }
    }
}
