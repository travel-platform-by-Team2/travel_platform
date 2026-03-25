package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class BookingApiControllerTest {

    @Test
    void rooms() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
        List<BookingResponse.RoomDTO> rooms = List.of(BookingResponse.RoomDTO.createRoom(
                "디럭스 룸",
                "오션뷰",
                "2",
                "4",
                "https://image.test/room.jpg",
                List.of()));

        when(bookingService.getRoomList(
                argThat((BookingRequest.RoomQueryDTO reqDTO) -> reqDTO != null
                        && "시그니엘 부산".equals(reqDTO.getLodgingName())
                        && "부산 해운대구".equals(reqDTO.getAddress()))))
                .thenReturn(rooms);

        ResponseEntity<Resp<List<BookingResponse.RoomDTO>>> response = controller.getRooms("시그니엘 부산", "부산 해운대구");

        assertSame(rooms, response.getBody().getBody());
    }

    @Test
    void img() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
        BookingResponse.PlaceImageDTO dto = BookingResponse.PlaceImageDTO.createPlaceImage("https://image.test/place.jpg", "해운대");

        when(bookingService.getPlaceImage(
                argThat((BookingRequest.PlaceImageQueryDTO reqDTO) -> reqDTO != null
                        && "https://place.map.kakao.com/1".equals(reqDTO.getPlaceUrl())
                        && "해운대".equals(reqDTO.getName())
                        && "부산 해운대구".equals(reqDTO.getAddress()))))
                .thenReturn(dto);

        ResponseEntity<Resp<BookingResponse.PlaceImageDTO>> response = controller.getPlaceImage(
                "https://place.map.kakao.com/1",
                "해운대",
                "부산 해운대구");

        assertSame(dto, response.getBody().getBody());
    }

    @Test
    void merge() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
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

        ResponseEntity<Resp<List<BookingResponse.MapPoiDTO>>> response = controller.mergeMapPois(reqDTO);

        assertSame(pois, response.getBody().getBody());
    }

    @Test
    void create() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
        BookingRequest.CreateBookingDTO reqDTO = new BookingRequest.CreateBookingDTO();

        ResponseEntity<Resp<Void>> response = controller.createBooking(reqDTO);

        verify(bookingService).createBooking(1, reqDTO);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void cancel() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());

        ResponseEntity<Resp<Void>> response = controller.cancelBooking(33);

        verify(bookingService).cancelBooking(1, 33);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void list() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
        List<BookingResponse.BookingSummaryDTO> list = List.of(BookingResponse.BookingSummaryDTO.builder()
                .id(1)
                .statusCode("booked")
                .statusLabel("예약 확정")
                .build());

        when(bookingService.getBookingList(1)).thenReturn(list);

        ResponseEntity<Resp<List<BookingResponse.BookingSummaryDTO>>> response = controller.getBookingList();

        verify(bookingService).getBookingList(1);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void detail() {
        BookingService bookingService = mock(BookingService.class);
        BookingApiController controller = new BookingApiController(bookingService, session());
        BookingResponse.BookingDetailDTO detail = BookingResponse.BookingDetailDTO.builder()
                .id(77)
                .checkIn(LocalDate.of(2026, 4, 10))
                .checkOut(LocalDate.of(2026, 4, 12))
                .statusCode("booked")
                .statusLabel("예약 확정")
                .createdAt(LocalDateTime.of(2026, 3, 21, 10, 0))
                .build();

        when(bookingService.getBookingDetail(1, 77)).thenReturn(detail);

        ResponseEntity<Resp<BookingResponse.BookingDetailDTO>> response = controller.getBookingDetail(77);

        verify(bookingService).getBookingDetail(1, 77);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void create401() {
        BookingApiController controller = new BookingApiController(mock(BookingService.class), new MockHttpSession());

        Assertions.assertThrows(Exception401.class,
                () -> controller.createBooking(new BookingRequest.CreateBookingDTO()));
    }

    private MockHttpSession session() {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(1, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
