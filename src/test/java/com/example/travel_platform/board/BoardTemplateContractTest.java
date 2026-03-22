package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BoardTemplateContractTest {

    private static final Path ROOT = Path.of("src/main/resources/templates/pages");

    @Test
    void listPage() throws IOException {
        String body = Files.readString(ROOT.resolve("board-list.mustache"));

        assertTrue(body.contains("{{#models}}"));
        assertTrue(body.contains("{{model.sortLabel}}"));
        assertFalse(body.contains("{{#page.boards}}"));
    }

    @Test
    void detailPage() throws IOException {
        String body = Files.readString(ROOT.resolve("board-detail.mustache"));

        assertTrue(body.contains("{{#model}}"));
        assertTrue(body.contains("{{#canManage}}"));
        assertFalse(body.contains("{{#page}}"));
    }

    @Test
    void formPages() throws IOException {
        String createBody = Files.readString(ROOT.resolve("board-create.mustache"));
        String editBody = Files.readString(ROOT.resolve("board-edit.mustache"));

        assertTrue(createBody.contains("{{#model}}"));
        assertTrue(editBody.contains("{{#model}}"));
    }
}
