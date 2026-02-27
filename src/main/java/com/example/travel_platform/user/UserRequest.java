package com.example.travel_platform.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class UserRequest {

    @Data
    public static class JoinDTO {
        @NotBlank(message = "유저네임을 입력해주세요")
        private String username;
        @NotBlank(message = "패스워드를 입력해주세요")
        private String password;
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @NotBlank
        private String email;
    }

    @Data
    public static class LoginDTO {
        @NotBlank(message = "유저 이름을 입력해주세요")
        private String username;
        @NotBlank(message = "패스워드를 입력해주세요")
        private String password;
    }
}
