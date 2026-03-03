package com.example.travel_platform._core.handler.ex;

// 권한 없음
public class Exception403 extends RuntimeException {
    public Exception403(String message) {
        super(message);
    }
}
