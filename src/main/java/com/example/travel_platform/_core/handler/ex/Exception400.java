package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

// 유효성 검사 실패 시 / 중복
public class Exception400 extends StatusException {

    public Exception400(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
