package com.example.travel_platform._core.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.StatusException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                "API_BAD_REQUEST",
                resolveValidationMessage(e));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return createErrorResponseEntity(HttpStatus.BAD_REQUEST, "API_BAD_REQUEST", "Request JSON body is invalid.");
    }

    @ExceptionHandler(StatusException.class)
    public ResponseEntity<ApiErrorResponse> handleStatusException(StatusException e) {
        return createErrorResponseEntity(e.getStatus(), resolveApiErrorCode(e), e.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException e) {
        return createErrorResponseEntity(resolveStatus(e), resolveCode(e), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception e) {
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "API_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    private String resolveValidationMessage(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        if (fieldError == null) {
            return "잘못된 요청 값입니다.";
        }
        return fieldError.getDefaultMessage();
    }

    private HttpStatus resolveStatus(ApiException e) {
        return e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus();
    }

    private String resolveCode(ApiException e) {
        return e.getCode() == null ? "API_INTERNAL_ERROR" : e.getCode();
    }

    private String resolveApiErrorCode(StatusException e) {
        return switch (e.getStatus()) {
            case BAD_REQUEST -> "API_BAD_REQUEST";
            case UNAUTHORIZED -> "API_UNAUTHORIZED";
            case FORBIDDEN -> "API_FORBIDDEN";
            case NOT_FOUND -> "API_NOT_FOUND";
            default -> "API_INTERNAL_ERROR";
        };
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponseEntity(HttpStatus status, String code, String message) {
        ApiErrorResponse body = ApiErrorResponse.createErrorResponse(code, message, status.value());
        return ResponseEntity.status(status).body(body);
    }
}
