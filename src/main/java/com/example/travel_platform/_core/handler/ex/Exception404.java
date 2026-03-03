package com.example.travel_platform._core.handler.ex;

// 자원을 찾을 수 없음
public class Exception404 extends RuntimeException {
    public Exception404(String message) {
        super(message);
    }
}
