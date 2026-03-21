package com.example.travel_platform._core.handler.ex;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class StatusException extends RuntimeException {

    private final HttpStatus status;

    protected StatusException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
