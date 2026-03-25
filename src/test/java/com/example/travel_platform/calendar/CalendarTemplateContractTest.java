package com.example.travel_platform.calendar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CalendarTemplateContractTest {

    private static final Path TEMPLATE_DIR = Path.of("src/main/resources/templates/pages");

    @Test
    void page() throws IOException {
        String template = template("calendar.mustache");

        assertContains(template, "<title>{{model.pageTitle}}</title>");
        assertContains(template, "data-calendar-grid");
        assertContains(template, "data-calendar-event-panel");
        assertContains(template, "data-calendar-memo-list");
    }

    private String template(String fileName) throws IOException {
        return Files.readString(TEMPLATE_DIR.resolve(fileName));
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), expected);
    }
}
