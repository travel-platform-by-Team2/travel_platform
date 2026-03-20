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

        assertContains(template, "{{#page.passwordSuccessMessage}}");
        assertContains(template, "{{#page.hasBookings}}");
        assertContains(template, "{{^page.hasBookings}}");
        assertContains(template, "{{#page.hasTripPlans}}");
        assertContains(template, "{{^page.hasTripPlans}}");
        assertContains(template, "{{#page.passwordError}}");
        assertContains(template, "{{#page.withdrawError}}");
        assertContains(template, "{{^page.passwordModalOpen}}is-hidden{{/page.passwordModalOpen}}");
        assertContains(template, "{{^page.withdrawModalOpen}}is-hidden{{/page.withdrawModalOpen}}");
    }

    @Test
    void bookingDetail() throws IOException {
        String template = template("booking-detail.mustache");

        assertContains(template, "{{page.bookingId}}");
        assertContains(template, "{{page.placeholderNotice}}");
        assertContains(template, "href=\"{{page.backLink}}\"");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
