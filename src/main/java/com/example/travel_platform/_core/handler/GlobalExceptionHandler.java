package com.example.travel_platform._core.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform._core.handler.ex.Exception500;

// 데이터를 찾아 응답)
@RestControllerAdvice // 모든 예외를 처리는 클래스 (Data응답)
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = Exception400.class) // 어떤 예외인지 지정하기
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ex400(Exception400 e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    history.back();
                </script>
                """, e.getMessage());
        return html;
    }

    @ExceptionHandler(exception = Exception401.class) // 어떤 예외인지 지정하기
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String ex401(Exception401 e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    location.href = '/login-form';
                </script>
                """, e.getMessage());
        return html;
    }

    @ExceptionHandler(exception = Exception403.class) // 어떤 예외인지 지정하기
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String ex403(Exception403 e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    history.back();
                </script>
                """, e.getMessage());
        return html;
    }

    @ExceptionHandler(exception = Exception404.class) // 어떤 예외인지 지정하기
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String ex404(Exception404 e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    history.back();
                </script>
                """, e.getMessage());
        return html;
    }

    @ExceptionHandler(exception = Exception500.class) // 어떤 예외인지 지정하기
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String ex500(Exception500 e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    history.back();
                </script>
                """, e.getMessage());
        return html;
    }

    @ExceptionHandler(exception = Exception.class) // 어떤 예외인지 지정하기
    public String exUnknown(Exception e) {
        String html = String.format("""
                <script>
                    alert('%s');
                    history.back();
                </script>
                """, "관리자에게 문의하세요");

        // 1. 로그
        System.out.println(e.getMessage());
        // 2. SMS 알림
        return html;
    }

}
