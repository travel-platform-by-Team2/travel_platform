package com.example.travel_platform._core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;

class ExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    private final ApiExceptionHandler apiExceptionHandler = new ApiExceptionHandler();

    @Test
    void mvcBack() {
        String body = globalExceptionHandler.handleBadRequest(new Exception400("잘못된 요청입니다."));

        assertTrue(body.contains("alert('잘못된 요청입니다.');"));
        assertTrue(body.contains("history.back();"));
    }

    @Test
    void mvcLogin() {
        String body = globalExceptionHandler.handleUnauthorized(new Exception401("로그인이 필요합니다."));

        assertTrue(body.contains("alert('로그인이 필요합니다.');"));
        assertTrue(body.contains("location.href='/login-form';"));
    }

    @Test
    void api401() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionHandler.handleStatusException(new Exception401("로그인이 필요합니다."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("API_UNAUTHORIZED", response.getBody().getCode());
        assertEquals("로그인이 필요합니다.", response.getBody().getMessage());
    }

    @Test
    void api500() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionHandler.handleUnexpectedException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("API_INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("서버 내부 오류가 발생했습니다.", response.getBody().getMessage());
    }

    @Test
    void apiCustom() {
        ResponseEntity<ApiErrorResponse> response = apiExceptionHandler.handleApiException(
                new ApiException("CHATBOT_INTERNAL_ERROR", "llm fail", HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("CHATBOT_INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("llm fail", response.getBody().getMessage());
    }

    @Test
    void status403() {
        Exception403 exception = new Exception403("권한이 없습니다.");

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void mvcNoRes() {
        String body = globalExceptionHandler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "/missing", ""));

        assertEquals(null, body);
    }
}
