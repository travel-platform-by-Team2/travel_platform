package com.example.travel_platform.core.handler.ex;

// 인증 실패 시
public class Exception401 extends RuntimeException {
    public Exception401(String message) {
        super(message);
    }
}
