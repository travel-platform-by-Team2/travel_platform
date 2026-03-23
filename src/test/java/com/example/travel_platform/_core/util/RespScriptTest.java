package com.example.travel_platform._core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RespScriptTest {

    @Test
    void respOk() {
        ResponseEntity<Resp<String>> response = Resp.ok("ok");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("성공", response.getBody().getMsg());
        assertEquals("ok", response.getBody().getBody());
    }

    @Test
    void respFail() {
        ResponseEntity<Resp<Void>> response = Resp.fail(HttpStatus.BAD_REQUEST, "실패");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("실패", response.getBody().getMsg());
        assertEquals(null, response.getBody().getBody());
    }

    @Test
    void scriptBack() {
        String body = Script.back("다시 시도해 주세요.");

        assertTrue(body.contains("alert('다시 시도해 주세요.');"));
        assertTrue(body.contains("history.back();"));
    }

    @Test
    void scriptHrefEscape() {
        String body = Script.href("/login-form", "로그인 '다시'\n필요");

        assertTrue(body.contains("alert('로그인 \\'다시\\'\\n필요');"));
        assertTrue(body.contains("location.href='/login-form';"));
    }
}
