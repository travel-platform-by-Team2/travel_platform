package com.example.travel_platform.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AdminTemplateContractTest {

    @Test
    void dash() throws IOException {
        String template = template("admin-dashboard.mustache");

        assertContains(template, "{{#model}}");
        assertContains(template, "{{>partials/admin-sidebar}}");
        assertContains(template, "{{#model.metrics}}");
        assertFalse(template.contains("{{#page}}"), "{{#page}}");
    }

    @Test
    void users() throws IOException {
        String template = template("admin-users.mustache");

        assertContains(template, "{{model.totalUserCount}}");
        assertContains(template, "{{model.inactiveUserCount}}");
        assertContains(template, "{{#models}}");
        assertContains(template, "{{model.keyword}}");
        assertContains(template, "{{model.sortBy}}");
        assertContains(template, "{{model.orderBy}}");
        assertFalse(template.contains("{{page.totalUserCount}}"), "{{page.totalUserCount}}");
    }

    @Test
    void boards() throws IOException {
        String template = template("admin-boards.mustache");

        assertContains(template, "{{model.allCount}}");
        assertContains(template, "{{#models}}");
        assertContains(template, "{{#model.pageItems}}");
        assertContains(template, "{{createdDate}}");
        assertFalse(template.contains("{{page."), "{{page.");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
