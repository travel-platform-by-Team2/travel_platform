package com.example.travel_platform._core.handler;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {

    private String code;
    private String message;
    private Integer status;
    private LocalDateTime timestamp;

    public static ApiErrorResponse createErrorResponse(String code, String message, Integer status) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
