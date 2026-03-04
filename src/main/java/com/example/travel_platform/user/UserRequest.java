package com.example.travel_platform.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 사용자 인증/회원가입 요청 바디를 정의하는 DTO 클래스
public class UserRequest {

    // 회원가입 요청 데이터를 담는 DTO
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

    // 로그인 요청 데이터를 담는 DTO
    @Data
    public static class LoginDTO {
        @NotBlank(message = "이메일을 입력해주세요")
        private String email;
        @NotBlank(message = "패스워드를 입력해주세요")
        private String password;
    }
}
