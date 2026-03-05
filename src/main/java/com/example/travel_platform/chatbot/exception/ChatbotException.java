package com.example.travel_platform.chatbot.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ChatbotException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ChatbotException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public ChatbotException(String code, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }
}
