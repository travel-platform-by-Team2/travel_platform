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
    public ResponseEntity<?> cancelBooking(@PathVariable("bookingId") Integer bookingId) {
        bookingService.cancelBooking(1, bookingId);
        return Resp.ok(null);
    }

    @GetMapping
    public ResponseEntity<?> getBookingList() {
        List<BookingResponse.BookingSummaryDTO> bookingList = bookingService.getBookingList(1);
        return Resp.ok(bookingList);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetail(@PathVariable("bookingId") Integer bookingId) {
        BookingResponse.BookingDetailDTO detail = bookingService.getBookingDetail(1, bookingId);
        return Resp.ok(detail);
    }

    /**
     * 프론트엔드(map-detail.js)에서 배열 형태를 직접 기대하므로, 
     * Resp로 감싸지 않고 직접 리스트를 반환합니다.
     */
    @GetMapping("/rooms")
    public List<BookingResponse.RoomDTO> getRooms(
            @RequestParam(name = "lodgingName") String lodgingName,
            @RequestParam(name = "address") String address) {
        return bookingService.fetchRoomsFromTourApi(this.tourApiServiceKey, lodgingName, address);
    }

    /**
     * 프론트엔드 호환성을 위해 DTO를 직접 반환합니다.
     */
    @GetMapping("/place-image")
    public BookingResponse.PlaceImageDTO getPlaceImage(
            @RequestParam(value = "placeUrl", required = false) String placeUrl,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address) {
        return bookingService.getPlaceImage(this.tourApiServiceKey, placeUrl, name, address);
    }

    /**
     * 프론트엔드(map-detail.js) 호환성을 위해 DTO를 직접 반환합니다.
     */
    @PostMapping("/map-pois/merge")
    public List<BookingResponse.MapPoiDTO> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        return bookingService.mergeMapPois(reqDTO);
    }
}
