package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class UserTemplateContractTest {

    private static final Path TEMPLATE_DIR = Path.of("src/main/resources/templates/pages");

    @Test
    void home() throws IOException {
        String template = template("main-index.mustache");

        assertContains(template, "<title>TravelMate | 홈</title>");
        assertContains(template, "form class=\"main-index-form-01\" action=\"/bookings/map-detail\" method=\"get\"");
        assertContains(template, "id=\"region\"");
        assertContains(template, "id=\"startDate\"");
        assertContains(template, "id=\"endDate\"");
        assertContains(template, "id=\"guests\"");
    }

    @Test
    void login() throws IOException {
        String template = template("login.mustache");

        assertContains(template, "<title>TravelMate | 로그인</title>");
        assertContains(template, "form class=\"login-form-01\" action=\"/login\" method=\"post\"");
        assertContains(template, "name=\"email\"");
        assertContains(template, "name=\"password\"");
    }

    @Test
    void join() throws IOException {
        String template = template("signup.mustache");

        assertContains(template, "<title>TravelMate | 회원가입</title>");
        assertContains(template, "form action=\"/join\" method=\"post\"");
        assertContains(template, "name=\"username\"");
        assertContains(template, "name=\"email\"");
        assertContains(template, "name=\"tel\"");
        assertContains(template, "name=\"password\"");
    }

    private String template(String fileName) throws IOException {
        return Files.readString(TEMPLATE_DIR.resolve(fileName));
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), expected);
    }
}
