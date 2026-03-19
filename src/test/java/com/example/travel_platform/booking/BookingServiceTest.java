package com.example.travel_platform.booking;

import java.util.List;

import org.junit.jupiter.api.Test;

public class BookingServiceTest {

    private final String serviceKey = "A6g65ZteBO8qpJ5z4rQkSPkVltHPXfnLNCa5Q7dlz6B1zFAi7UGM251/sAcsYRAef/AeWI6Yy7SYLQZDyAkBmg==";

    @Test
    public void fetchRoomsFromTourApi_test() {
        BookingService bookingService = new BookingService(null, null, null);
        String lodgingName = "테스트 숙소";
        String address = "부산";

        List<BookingResponse.RoomDTO> rooms = bookingService.fetchRoomsFromTourApi(serviceKey, lodgingName, address);

        System.out.println("=======================");
        System.out.println("rooms count = " + (rooms == null ? 0 : rooms.size()));
        if (rooms != null) {
            for (BookingResponse.RoomDTO room : rooms) {
                System.out.println("room name = " + room.getName());
                System.out.println("imageUrl = " + room.getImageUrl());
                System.out.println("content = " + room.getContent());
                System.out.println("-----------------------");
            }
        }
    }

    @Test
    public void fetchImageFromTourApi_test() {
        System.out.println("skipped (repository wiring needed)");
    }
}

