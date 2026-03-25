package com.example.travel_platform.user;

import lombok.Data;

public class UserResponse {

    @Data
    public static class LoginPageModelDTO {
        private String kakaoJsAppKey;
        private String naverClientId;
        private String googleClientId;

        public static LoginPageModelDTO createLoginPageModel(
                String kakaoJsAppKey,
                String naverClientId,
                String googleClientId) {
            LoginPageModelDTO model = new LoginPageModelDTO();
            model.setKakaoJsAppKey(kakaoJsAppKey);
            model.setNaverClientId(naverClientId);
            model.setGoogleClientId(googleClientId);
            return model;
        }
    }
}
