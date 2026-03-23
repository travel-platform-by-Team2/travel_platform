package com.example.travel_platform.user;

import java.util.Arrays;

public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static UserRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user role: " + code));
    }

    public static UserRole fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(role -> role.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
