package com.example.travel_platform._core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;

class ExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    private final ApiExceptionHandler apiExceptionHandler = new ApiExceptionHandler();

    @Test
    void mvcBack() {
        String body = globalExceptionHandler.ex400(new Exception400("잘못된 요청입니다."));

        assertTrue(body.contains("alert('잘못된 요청입니다.');"));
        assertTrue(body.contains("history.back();"));
    }

    @Test
    void mvcLogin() {
        String body = globalExceptionHandler.ex401(new Exception401("로그인이 필요합니다."));

        assertTrue(body.contains("alert('로그인이 필요합니다.');"));
        assertTrue(body.contains("location.href='/login-form';"));
    }

    @Test
    void api401() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionHandler.handleException401(new Exception401("로그인이 필요합니다."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("API_UNAUTHORIZED", response.getBody().getCode());
        assertEquals("로그인이 필요합니다.", response.getBody().getMessage());
    }

    @Test
    void api500() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionHandler.handleUnknown(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("API_INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("서버 내부 오류가 발생했습니다.", response.getBody().getMessage());
    }
}
