package com.example.travel_platform.core.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import com.example.travel_platform.core.handler.ex.Exception400;

@Aspect
@Component
public class ValidatinoHandler {

    @Before("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void ValidationCheck(JoinPoint jp) {
        for (Object arg : jp.getArgs()) {
            if (arg instanceof Errors errors) {
                if (errors.hasErrors()) {
                    throw new Exception400(errors.getAllErrors().get(0).getDefaultMessage());
                }
            }
        }
    }
}
