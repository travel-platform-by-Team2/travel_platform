package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

// 인증 실패 시
public class Exception401 extends StatusException {
    public Exception401(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
