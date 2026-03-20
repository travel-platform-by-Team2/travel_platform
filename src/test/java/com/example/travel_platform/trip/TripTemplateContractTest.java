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

        assertContains(template, "{{#page}}");
        assertContains(template, "{{#plans}}");
        assertContains(template, "{{category}}");
    }

    @Test
    void createPage() throws IOException {
        String template = template("trip-create.mustache");

        assertContains(template, "value=\"{{page.title}}\"");
        assertContains(template, "data-selected-region=\"{{page.region}}\"");
        assertContains(template, "value=\"{{page.whoWith}}\"");
        assertContains(template, "{{#page.titleError}}");
        assertContains(template, "{{#page.startDateError}}");
    }

    @Test
    void detailPlan() throws IOException {
        String template = template("trip-detail.mustache");

        assertContains(template, "{{#plan}}{{plan.formattedTitle}}{{/plan}}");
        assertContains(template, "{{#plan}}{{plan.regionLabel}}{{/plan}}");
        assertContains(template, "{{#plan}}{{plan.travelPeriodLabel}}{{/plan}}");
    }

    @Test
    void placePage() throws IOException {
        String template = template("trip-add-place.mustache");

        assertContains(template, "data-existing-count=\"{{existingCount}}\"");
        assertContains(template, "data-save-url=\"{{saveUrl}}\"");
        assertContains(template, "data-detail-url=\"{{detailUrl}}\"");
        assertContains(template, "{{#page.kakaoMapAppKey}}");
    }

    private String template(String fileName) throws IOException {
        return Files.readString(TEMPLATE_DIR.resolve(fileName));
    }

    private void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), expected);
    }
}
