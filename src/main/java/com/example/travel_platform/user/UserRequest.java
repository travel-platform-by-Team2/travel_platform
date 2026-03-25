package com.example.travel_platform.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserRequest {

    @Data
    public static class JoinDTO {
        @Size(max = 6, message = "닉네임은 최대 6글자까지 가능합니다.")
        @Pattern(regexp = "^[가-힣]+$", message = "한글 이름만 가능합니다.")
        private String username;
        private String password;
        private String email;
        private String tel;
    }

    @Data
    public static class LoginDTO {
        private String email;
        private String password;
    }

    @Data
    public static class SnsCallbackDTO {
        private String email;
        private String nickname;
        private String providerId;
    }
}
