package com.example.travel_platform._core.template;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class StaticStyleContractTest {

    private static final Path TEMPLATE_DIR = Path.of("src/main/resources/templates");
    private static final Path PAGE_DIR = TEMPLATE_DIR.resolve("pages");

    @Test
    void common() throws IOException {
        String headAssets = template("partials/head-assets.mustache");
        String chatbotAssets = template("partials/chatbot-assets.mustache");

        assertNotContains(headAssets, "/css/chatbot.css");
        assertContains(chatbotAssets, "/css/chatbot.css");
    }

    @Test
    void chatbotPages() throws IOException {
        try (Stream<Path> pages = Files.list(PAGE_DIR)) {
            pages.filter(path -> path.getFileName().toString().endsWith(".mustache"))
                    .forEach(this::assertChatbotAssetContract);
        }
    }

    private void assertChatbotAssetContract(Path pagePath) {
        try {
            String template = Files.readString(pagePath);
            boolean hasChatbot = template.contains("{{>partials/chatbot}}");
            boolean hasChatbotAssets = template.contains("{{>partials/chatbot-assets}}");

            if (hasChatbot) {
                assertTrue(hasChatbotAssets, pagePath.getFileName().toString());
                return;
            }
            assertFalse(hasChatbotAssets, pagePath.getFileName().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
