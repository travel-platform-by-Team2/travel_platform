package com.example.travel_platform._core.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform._core.handler.ex.Exception500;
import com.example.travel_platform._core.util.Script;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = Exception400.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception400 e) {
        return createBackScript(e.getMessage());
    }

    @ExceptionHandler(exception = Exception401.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorized(Exception401 e) {
        return createLoginRedirectScript(e.getMessage());
    }

    @ExceptionHandler(exception = Exception403.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(Exception403 e) {
        return createBackScript(e.getMessage());
    }

    @ExceptionHandler(exception = Exception404.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception404 e) {
        return createBackScript(e.getMessage());
    }

    @ExceptionHandler(exception = Exception500.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalError(Exception500 e) {
        return createBackScript(e.getMessage());
    }

    @ExceptionHandler(exception = NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(NoResourceFoundException e) {
        return null;
    }

    @ExceptionHandler(exception = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e) {
        e.printStackTrace();
        return createBackScript("관리자에게 문의하세요");
    }

    private String createBackScript(String message) {
        return Script.back(message);
    }

    private String createLoginRedirectScript(String message) {
        return Script.href("/login-form", message);
    }
}
