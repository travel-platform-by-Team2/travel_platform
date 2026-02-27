package com.example.travel_platform.user;

import lombok.Data;

public class UserRequest {

    @Data
    public static class SaveDTO {
        private String username;
        private String password;
        private String email;

        // 객체 생성
        public User toEntity() {
            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }
    }

    @Data
    public static class LoginDTO {
        private String username;
        private String password;
    }
}