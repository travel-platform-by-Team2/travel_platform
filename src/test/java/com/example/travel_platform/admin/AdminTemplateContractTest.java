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

        assertContains(template, "{{#page}}");
        assertContains(template, "{{>partials/admin-sidebar}}");
        assertContains(template, "{{#page.metrics}}");
    }

    @Test
    void users() throws IOException {
        String template = template("admin-users.mustache");

        assertContains(template, "{{page.totalUserCount}}");
        assertContains(template, "{{page.inactiveUserCount}}");
        assertContains(template, "{{#page.users}}");
        assertContains(template, "{{page.keyword}}");
        assertFalse(template.contains("{{totalUserCount}}"), "{{totalUserCount}}");
    }

    @Test
    void boards() throws IOException {
        String template = template("admin-boards.mustache");

        assertContains(template, "{{page.allCount}}");
        assertContains(template, "{{#page.boards}}");
        assertContains(template, "{{#page.pageItems}}");
        assertContains(template, "{{createdDate}}");
        assertFalse(template.contains("{{model."), "{{model.");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
