package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardCategoryTest {

    @Test
    void code() {
        assertEquals(BoardCategory.TIPS, BoardCategory.fromCode("tips"));
        assertEquals(BoardCategory.QNA, BoardCategory.fromCode("qna"));
        assertNull(BoardCategory.fromCodeOrNull("unknown"));
        assertTrue(BoardCategory.isValidCode("review"));
        assertFalse(BoardCategory.isValidCode("unknown"));
    }
}
