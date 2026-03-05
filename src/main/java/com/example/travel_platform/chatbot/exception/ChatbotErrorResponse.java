package com.example.travel_platform.chatbot.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatbotErrorResponse {

    private String code;
    private String message;
    private Integer status;
    private LocalDateTime timestamp;

    public static ChatbotErrorResponse of(String code, String message, Integer status) {
        return ChatbotErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
