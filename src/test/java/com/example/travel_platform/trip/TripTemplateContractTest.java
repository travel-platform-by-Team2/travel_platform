package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TripTemplateContractTest {

    private static final Path TEMPLATE_DIR = Path.of("src/main/resources/templates/pages");

    @Test
    void listPage() throws IOException {
        String template = template("trip-list.mustache");

        assertContains(template, "{{#model}}");
        assertContains(template, "{{#models}}");
        assertContains(template, "{{category}}");
    }

    @Test
    void createPage() throws IOException {
        String template = template("trip-create.mustache");

        assertContains(template, "value=\"{{model.title}}\"");
        assertContains(template, "data-selected-region=\"{{model.region}}\"");
        assertContains(template, "value=\"{{model.whoWith}}\"");
        assertContains(template, "value=\"{{model.startDateValue}}\"");
        assertContains(template, "value=\"{{model.endDateValue}}\"");
        assertContains(template, "{{#model.titleError}}");
        assertContains(template, "{{#model.startDateError}}");
    }

    @Test
    void detailPage() throws IOException {
        String template = template("trip-detail.mustache");

        assertContains(template, "{{#model}}{{formattedTitle}}{{/model}}");
        assertContains(template, "{{#model}}{{regionLabel}}{{/model}}");
        assertContains(template, "{{#model}}{{travelPeriodLabel}}{{/model}}");
    }

    @Test
    void placePage() throws IOException {
        String template = template("trip-add-place.mustache");

        assertContains(template, "{{#model}}");
        assertContains(template, "data-existing-count=\"{{existingCount}}\"");
        assertContains(template, "data-save-url=\"{{saveUrl}}\"");
        assertContains(template, "data-detail-url=\"{{detailUrl}}\"");
        assertContains(template, "{{#model.kakaoMapAppKey}}");
    }

    private String template(String fileName) throws IOException {
        return Files.readString(TEMPLATE_DIR.resolve(fileName));
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), expected);
    }
}
