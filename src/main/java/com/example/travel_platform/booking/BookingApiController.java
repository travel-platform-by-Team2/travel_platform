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

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingService bookingService;
    private final String tourApiServiceKey;

    public BookingApiController(
            BookingService bookingService,
            @Value("${TOUR_API_SERVICE_KEY:}") String tourApiServiceKey) {
        this.bookingService = bookingService;
        this.tourApiServiceKey = tourApiServiceKey;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest.CreateBookingDTO reqDTO) {
        bookingService.createBooking(1, reqDTO);
        return Resp.ok(null);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable(name = "bookingId") Integer bookingId) {
        bookingService.cancelBooking(1, bookingId);
        return Resp.ok(null);
    }

    @GetMapping
    public ResponseEntity<?> getBookingList() {
        List<BookingResponse.BookingSummaryDTO> bookingList = bookingService.getBookingList(1);
        return Resp.ok(bookingList);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetail(@PathVariable(name = "bookingId") Integer bookingId) {
        BookingResponse.BookingDetailDTO detail = bookingService.getBookingDetail(1, bookingId);
        return Resp.ok(detail);
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms(
            @RequestParam(name = "lodgingName") String lodgingName,
            @RequestParam(name = "address") String address) {
        List<BookingResponse.RoomDTO> rooms = bookingService.fetchRoomsFromTourApi(this.tourApiServiceKey, lodgingName, address);
        return Resp.ok(rooms);
    }

    @GetMapping("/place-image")
    public ResponseEntity<?> getPlaceImage(
            @RequestParam(name = "placeUrl", required = false) String placeUrl,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "address", required = false) String address) {
        BookingResponse.PlaceImageDTO imageDTO = bookingService.getPlaceImage(this.tourApiServiceKey, placeUrl, name, address);
        return Resp.ok(imageDTO);
    }

    @PostMapping("/map-pois/merge")
    public ResponseEntity<?> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingResponse.MapPoiDTO> items = bookingService.mergeMapPois(reqDTO);
        return Resp.ok(items);
    }
}
