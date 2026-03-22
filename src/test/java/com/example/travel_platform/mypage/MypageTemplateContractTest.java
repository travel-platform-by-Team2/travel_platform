package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MypageTemplateContractTest {

    @Test
    void mainPage() throws IOException {
        String template = template("mypage.mustache");

        assertContains(template, "{{model.bookingSection.listLink}}");
        assertContains(template, "{{#model.passwordSuccessMessage}}");
        assertContains(template, "{{model.profile.username}}");
        assertContains(template, "{{#model.bookingSection.hasItems}}");
        assertContains(template, "{{^model.bookingSection.hasItems}}");
        assertContains(template, "{{#model.tripPlanSection.hasItems}}");
        assertContains(template, "{{^model.tripPlanSection.hasItems}}");
        assertContains(template, "{{#model.passwordError}}");
        assertContains(template, "{{#model.withdrawError}}");
    }

    @Test
    void bookingList() throws IOException {
        String template = template("booking-list.mustache");

        assertContains(template, "{{#model.allSelected}}");
        assertContains(template, "?category=upcoming");
        assertContains(template, "?category=completed");
        assertContains(template, "?category=cancelled");
        assertContains(template, "{{#models}}");
        assertContains(template, "{{#upcoming}}");
        assertContains(template, "{{#completed}}");
        assertContains(template, "{{#cancelled}}");
        assertContains(template, "{{detailLink}}");
        assertContains(template, "{{model.mypageLink}}");
    }

    @Test
    void bookingDetail() throws IOException {
        String template = template("booking-detail.mustache");

        assertContains(template, "{{model.bookingNumberText}}");
        assertContains(template, "{{model.bookingListLink}}");
        assertContains(template, "{{model.mypageLink}}");
        assertContains(template, "{{#model.cancelled}}");
        assertContains(template, "{{^model.cancelled}}");
        assertContains(template, "{{model.totalPriceText}}");
        assertContains(template, "{{model.lodgingName}}");
        assertContains(template, "data-cancel-url=\"{{model.cancelApiUrl}}\"");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
