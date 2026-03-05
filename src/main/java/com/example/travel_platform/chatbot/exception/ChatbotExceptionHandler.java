package com.example.travel_platform.chatbot.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.chatbot.ChatbotController;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ChatbotController.class)
public class ChatbotExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ChatbotErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request value.");
        return build(HttpStatus.BAD_REQUEST, "CHATBOT_BAD_REQUEST", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ChatbotErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "CHATBOT_BAD_REQUEST", "Request JSON body is invalid.");
    }

    @ExceptionHandler(Exception400.class)
    public ResponseEntity<ChatbotErrorResponse> handleException400(Exception400 e) {
        return build(HttpStatus.BAD_REQUEST, "CHATBOT_BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(ChatbotException.class)
    public ResponseEntity<ChatbotErrorResponse> handleChatbotException(ChatbotException e) {
        HttpStatus status = e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus();
        String code = e.getCode() == null ? "CHATBOT_INTERNAL_ERROR" : e.getCode();
        return build(status, code, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatbotErrorResponse> handleUnknown(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT_INTERNAL_ERROR", "An internal chatbot error occurred.");
    }

    private ResponseEntity<ChatbotErrorResponse> build(HttpStatus status, String code, String message) {
        ChatbotErrorResponse body = ChatbotErrorResponse.of(code, message, status.value());
        return ResponseEntity.status(status).body(body);
    }
}
