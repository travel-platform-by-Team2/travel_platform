package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

// 자원을 찾을 수 없음
public class Exception404 extends StatusException {
    public Exception404(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
