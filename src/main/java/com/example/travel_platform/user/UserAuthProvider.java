package com.example.travel_platform.user;

import java.util.Arrays;

public enum UserAuthProvider {
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    private final String code;

    UserAuthProvider(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserAuthProvider fromCode(String code) {
        return Arrays.stream(values())
                .filter(provider -> provider.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown auth provider: " + code));
    }

    public static UserAuthProvider fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(provider -> provider.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
