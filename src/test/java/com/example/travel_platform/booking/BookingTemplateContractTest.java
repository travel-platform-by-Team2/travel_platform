package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BookingTemplateContractTest {

    @Test
    void map() throws IOException {
        String template = template("map-detail.mustache");

        assertContains(template, "{{#model.kakaoMapAppKey}}");
        assertContains(template, "{{model.kakaoMapAppKey}}");
    }

    @Test
    void co() throws IOException {
        String template = template("booking-checkout.mustache");

        assertContains(template, "{{#model}}");
        assertContains(template, "value=\"{{lodgingName}}\"");
        assertContains(template, "value=\"{{bookerName}}\"");
        assertContains(template, "{{totalPriceText}}");
    }

    @Test
    void done() throws IOException {
        String template = template("booking-complete.mustache");

        assertContains(template, "{{#model}}");
        assertContains(template, "{{bookingNumber}}");
        assertContains(template, "value=\"{{regionKey}}\"");
        assertContains(template, "{{completeImageUrl}}");
    }

    private String template(String fileName) throws IOException {
        Path path = Path.of("src/main/resources/templates/pages", fileName);
        return Files.readString(path);
    }

    private void assertContains(String template, String expected) {
        assertTrue(template.contains(expected), expected);
    }
}
