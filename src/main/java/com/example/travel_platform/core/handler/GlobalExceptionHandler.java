package com.example.travel_platform.core.handler;

import org.springframework.web.bind.annotation.*;
import com.example.travel_platform.core.util.Script;
import com.example.travel_platform.core.handler.ex.Exception401;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String myHandler(Exception e) {
        return Script.back(e.getMessage());
    }

    @ExceptionHandler(Exception401.class)
    @ResponseBody
    public String myHandler401(Exception401 e) {
        return Script.href("/login-form", e.getMessage());
    }

}
