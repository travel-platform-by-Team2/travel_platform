package com.example.travel_platform._core.util;

import lombok.Data;
import org.springframework.http.*;

@Data
public class Resp<T> {
    private Integer status;
    private String msg;
    private T body;

    public Resp(Integer status, String msg, T body) {
        this.status = status;
        this.msg = msg;
        this.body = body;
    }

    public static <B> ResponseEntity<Resp<B>> ok(B body) {
        Resp<B> responseBody = createBody(HttpStatus.OK.value(), "성공", body);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<Resp<Void>> fail(HttpStatus status, String msg) {
        Resp<Void> responseBody = createBody(status.value(), msg, null);
        return ResponseEntity.status(status).body(responseBody);
    }

    private static <B> Resp<B> createBody(Integer status, String msg, B body) {
        return new Resp<>(status, msg, body);
    }
}
