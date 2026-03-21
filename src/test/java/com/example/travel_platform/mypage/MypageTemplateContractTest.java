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

        assertContains(template, "{{#model.passwordSuccessMessage}}");
        assertContains(template, "{{#model.hasBookings}}");
        assertContains(template, "{{^model.hasBookings}}");
        assertContains(template, "{{#model.hasTripPlans}}");
        assertContains(template, "{{^model.hasTripPlans}}");
        assertContains(template, "{{#model.passwordError}}");
        assertContains(template, "{{#model.withdrawError}}");
        assertContains(template, "{{^model.passwordModalOpen}}is-hidden{{/model.passwordModalOpen}}");
        assertContains(template, "{{^model.withdrawModalOpen}}is-hidden{{/model.withdrawModalOpen}}");
    }

    @Test
    void bookingDetail() throws IOException {
        String template = template("booking-detail.mustache");

        assertContains(template, "{{model.bookingId}}");
        assertContains(template, "{{model.placeholderNotice}}");
        assertContains(template, "href=\"{{model.backLink}}\"");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
