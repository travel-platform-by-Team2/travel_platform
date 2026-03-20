package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class BookingApiControllerTest {

    @Test
    void rooms() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");
        List<BookingResponse.RoomDTO> rooms = List.of(BookingResponse.RoomDTO.of(
                "디럭스 룸",
                "오션뷰",
                "2",
                "4",
                "https://image.test/room.jpg",
                List.of()));

        when(bookingService.getRoomList(
                eq("tour-key"),
                argThat(reqDTO -> reqDTO != null
                        && "시그니엘 부산".equals(reqDTO.getLodgingName())
                        && "부산 해운대구".equals(reqDTO.getAddress()))))
                .thenReturn(rooms);

        List<BookingResponse.RoomDTO> response = controller.getRooms("시그니엘 부산", "부산 해운대구");

        assertSame(rooms, response);
    }

    @Test
    void img() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");
        BookingResponse.PlaceImageDTO dto = BookingResponse.PlaceImageDTO.of("https://image.test/place.jpg", "해운대");

        when(bookingService.getPlaceImage(
                eq("tour-key"),
                argThat(reqDTO -> reqDTO != null
                        && "https://place.map.kakao.com/1".equals(reqDTO.getPlaceUrl())
                        && "해운대".equals(reqDTO.getName())
                        && "부산 해운대구".equals(reqDTO.getAddress()))))
                .thenReturn(dto);

        BookingResponse.PlaceImageDTO response = controller.getPlaceImage(
                "https://place.map.kakao.com/1",
                "해운대",
                "부산 해운대구");

        assertSame(dto, response);
    }

    @Test
    void merge() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");
        BookingRequest.MergeMapPoisDTO reqDTO = new BookingRequest.MergeMapPoisDTO();
        List<BookingResponse.MapPoiDTO> pois = List.of(new BookingResponse.MapPoiDTO(
                "p1",
                "시그니엘 부산",
                "",
                "부산",
                "",
                "",
                "",
                "AD5",
                35.1,
                129.1,
                "hotel",
                "DB"));

        when(bookingService.mergeMapPois(reqDTO)).thenReturn(pois);

        List<BookingResponse.MapPoiDTO> response = controller.mergeMapPois(reqDTO);

        assertSame(pois, response);
    }

    @Test
    void create() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");
        BookingRequest.CreateBookingDTO reqDTO = new BookingRequest.CreateBookingDTO();

        ResponseEntity<?> response = controller.createBooking(reqDTO);

        verify(bookingService).createBooking(1, reqDTO);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void cancel() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");

        ResponseEntity<?> response = controller.cancelBooking(33);

        verify(bookingService).cancelBooking(1, 33);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void list() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");
        List<BookingResponse.BookingSummaryDTO> list = List.of();

        when(bookingService.getBookingList(1)).thenReturn(list);

        ResponseEntity<?> response = controller.getBookingList();

        verify(bookingService).getBookingList(1);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void detail() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, "tour-key");

        when(bookingService.getBookingDetail(1, 77)).thenReturn(null);

        ResponseEntity<?> response = controller.getBookingDetail(77);

        verify(bookingService).getBookingDetail(1, 77);
        assertEquals(200, response.getStatusCode().value());
    }
}
