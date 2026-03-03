package com.example.travel_platform._core.handler.ex;

// 유효성 검사 실패 시 / 중복
public class Exception400 extends RuntimeException {

    public Exception400(String message) {
        super(message);
    }

}