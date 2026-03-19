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
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform._core.handler.ex.Exception500;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청 값입니다.");
        return build(HttpStatus.BAD_REQUEST, "API_BAD_REQUEST", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "API_BAD_REQUEST", "Request JSON body is invalid.");
    }

    @ExceptionHandler(Exception400.class)
    public ResponseEntity<ApiErrorResponse> handleException400(Exception400 e) {
        return build(HttpStatus.BAD_REQUEST, "API_BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(Exception401.class)
    public ResponseEntity<ApiErrorResponse> handleException401(Exception401 e) {
        return build(HttpStatus.UNAUTHORIZED, "API_UNAUTHORIZED", e.getMessage());
    }

    @ExceptionHandler(Exception403.class)
    public ResponseEntity<ApiErrorResponse> handleException403(Exception403 e) {
        return build(HttpStatus.FORBIDDEN, "API_FORBIDDEN", e.getMessage());
    }

    @ExceptionHandler(Exception404.class)
    public ResponseEntity<ApiErrorResponse> handleException404(Exception404 e) {
        return build(HttpStatus.NOT_FOUND, "API_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(Exception500.class)
    public ResponseEntity<ApiErrorResponse> handleException500(Exception500 e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "API_INTERNAL_ERROR", e.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException e) {
        HttpStatus status = e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus();
        String code = e.getCode() == null ? "API_INTERNAL_ERROR" : e.getCode();
        return build(status, code, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "API_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message) {
        ApiErrorResponse body = ApiErrorResponse.of(code, message, status.value());
        return ResponseEntity.status(status).body(body);
    }
}
