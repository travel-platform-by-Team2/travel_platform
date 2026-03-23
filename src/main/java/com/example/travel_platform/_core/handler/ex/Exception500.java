package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

// 서버측 에러
public class Exception500 extends StatusException {
    public Exception500(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
