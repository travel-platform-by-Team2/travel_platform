package com.example.travel_platform.mypage;

import lombok.Data;

public class MypageRequest {

    @Data
    public static class ChangePasswordDTO {
        private String currentPassword;
        private String newPassword;
        private String newPasswordConfirm;
    }
}
