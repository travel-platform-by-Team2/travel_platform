package com.example.travel_platform._core.template;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class StaticScriptContractTest {

    private static final Path TEMPLATE_DIR = Path.of("src/main/resources/templates");

    @Test
    void common() throws IOException {
        String partial = template("partials/scripts.mustache");

        assertContains(partial, "/js/chatbot.js");
        assertNotContains(partial, "/js/calendar-add-event.js");
        assertNotContains(partial, "/js/main-index.js");
        assertNotContains(partial, "/js/map-detail.js");
    }

    @Test
    void calendar() throws IOException {
        String template = template("pages/calendar.mustache");

        assertContains(template, "<script src=\"/js/calendar-add-event.js\"></script>");
        assertContains(template, "{{>partials/scripts}}");
    }

    @Test
    void home() throws IOException {
        String template = template("pages/main-index.mustache");

        assertContains(template, "<script src=\"/js/main-index.js\"></script>");
        assertContains(template, "{{>partials/scripts}}");
    }

    @Test
    void map() throws IOException {
        String template = template("pages/map-detail.mustache");

        assertContains(template, "<script src=\"/js/map-detail.js\"></script>");
        assertContains(template, "{{>partials/scripts}}");
    }

    private String template(String fileName) throws IOException {
        return Files.readString(TEMPLATE_DIR.resolve(fileName));
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), expected);
    }

    private void assertNotContains(String content, String expected) {
        assertFalse(content.contains(expected), expected);
    }
}
