package com.example.travel_platform._core.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public String ex400(Exception400 e) {
        return back(e.getMessage());
    }

    @ExceptionHandler(exception = Exception401.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String ex401(Exception401 e) {
        return loginForm(e.getMessage());
    }

    @ExceptionHandler(exception = Exception403.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String ex403(Exception403 e) {
        return back(e.getMessage());
    }

    @ExceptionHandler(exception = Exception404.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ex404(Exception404 e) {
        return back(e.getMessage());
    }

    @ExceptionHandler(exception = Exception500.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String ex500(Exception500 e) {
        return back(e.getMessage());
    }

    @ExceptionHandler(exception = Exception.class)
    public String exUnknown(Exception e) {
        System.out.println(e.getMessage());
        return back("관리자에게 문의하세요");
    }

    private String back(String message) {
        return Script.back(message);
    }

    private String loginForm(String message) {
        return Script.href("/login-form", message);
    }
}
