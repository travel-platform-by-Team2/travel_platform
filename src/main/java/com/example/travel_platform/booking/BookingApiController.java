package com.example.travel_platform.booking;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingService bookingService;
    private final String tourApiServiceKey;
    private final HttpSession session;

    public BookingApiController(
            BookingService bookingService,
            @Value("${TOUR_API_SERVICE_KEY:}") String tourApiServiceKey,
            HttpSession session) {
        this.bookingService = bookingService;
        this.tourApiServiceKey = tourApiServiceKey;
        this.session = session;
    }

    @PostMapping
    public ResponseEntity<Resp<Void>> createBooking(@RequestBody BookingRequest.CreateBookingDTO reqDTO) {
        bookingService.createBooking(requireSessionUserId(), reqDTO);
        return Resp.ok(null);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Resp<Void>> cancelBooking(@PathVariable(name = "bookingId") Integer bookingId) {
        bookingService.cancelBooking(requireSessionUserId(), bookingId);
        return Resp.ok(null);
    }

    @GetMapping
    public ResponseEntity<Resp<List<BookingResponse.BookingSummaryDTO>>> getBookingList() {
        List<BookingResponse.BookingSummaryDTO> bookingList = bookingService.getBookingList(requireSessionUserId());
        return Resp.ok(bookingList);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Resp<BookingResponse.BookingDetailDTO>> getBookingDetail(
            @PathVariable(name = "bookingId") Integer bookingId) {
        BookingResponse.BookingDetailDTO detail = bookingService.getBookingDetail(requireSessionUserId(), bookingId);
        return Resp.ok(detail);
    }

    @GetMapping("/rooms")
    public ResponseEntity<Resp<List<BookingResponse.RoomDTO>>> getRooms(
            @RequestParam(name = "lodgingName") String lodgingName,
            @RequestParam(name = "address") String address) {
        List<BookingResponse.RoomDTO> rooms = bookingService.getRoomList(
                tourApiServiceKey,
                BookingRequest.RoomQueryDTO.builder()
                        .lodgingName(lodgingName)
                        .address(address)
                        .build());
        return Resp.ok(rooms);
    }

    @GetMapping("/place-image")
    public ResponseEntity<Resp<BookingResponse.PlaceImageDTO>> getPlaceImage(
            @RequestParam(name = "placeUrl", required = false) String placeUrl,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "address", required = false) String address) {
        BookingResponse.PlaceImageDTO image = bookingService.getPlaceImage(
                tourApiServiceKey,
                BookingRequest.PlaceImageQueryDTO.builder()
                        .placeUrl(placeUrl)
                        .name(name)
                        .address(address)
                        .build());
        return Resp.ok(image);
    }

    @PostMapping("/map-pois/merge")
    public ResponseEntity<Resp<List<BookingResponse.MapPoiDTO>>> mergeMapPois(
            @RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingResponse.MapPoiDTO> pois = bookingService.mergeMapPois(reqDTO);
        return Resp.ok(pois);
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }
}
