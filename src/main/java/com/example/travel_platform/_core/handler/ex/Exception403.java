package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

// 권한 없음
public class Exception403 extends StatusException {
    public Exception403(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
