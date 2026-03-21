package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TripCodeTest {

    @Test
    void region() {
        assertEquals(TripRegion.JEJU, TripRegion.fromCode("jeju"));
        assertEquals("제주", TripRegion.JEJU.getLabel());
        assertNull(TripRegion.fromCodeOrNull("unknown"));
        assertTrue(TripRegion.isValidCode("busan"));
        assertFalse(TripRegion.isValidCode("unknown"));
    }

    @Test
    void companion() {
        assertEquals(TripCompanionType.FRIEND, TripCompanionType.fromCode("friend"));
        assertEquals("친구와", TripCompanionType.FRIEND.getLabel());
        assertNull(TripCompanionType.fromCodeOrNull("unknown"));
        assertTrue(TripCompanionType.isValidCode("solo"));
        assertFalse(TripCompanionType.isValidCode("friend-with"));
    }
}
