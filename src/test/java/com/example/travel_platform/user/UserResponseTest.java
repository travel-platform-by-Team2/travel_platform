package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void loginPageModel() {
        UserResponse.LoginPageModelDTO model = UserResponse.LoginPageModelDTO.createLoginPageModel(
                "kakao-key",
                "naver-id",
                "google-id");

        assertEquals("kakao-key", model.getKakaoJsAppKey());
        assertEquals("naver-id", model.getNaverClientId());
        assertEquals("google-id", model.getGoogleClientId());
    }
}
