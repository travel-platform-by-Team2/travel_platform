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
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest.CreateBookingDTO reqDTO) {
        bookingService.createBooking(requireSessionUserId(), reqDTO);
        return Resp.ok(null);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable(name = "bookingId") Integer bookingId) {
        bookingService.cancelBooking(requireSessionUserId(), bookingId);
        return Resp.ok(null);
    }

    @GetMapping
    public ResponseEntity<?> getBookingList() {
        List<BookingResponse.BookingSummaryDTO> bookingList = bookingService.getBookingList(requireSessionUserId());
        return Resp.ok(bookingList);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetail(@PathVariable(name = "bookingId") Integer bookingId) {
        BookingResponse.BookingDetailDTO detail = bookingService.getBookingDetail(requireSessionUserId(), bookingId);
        return Resp.ok(detail);
    }

    private Integer requireSessionUserId() {
        // 실제 로그인이 필요한 경우 세션에서 ID를 가져옴
        return SessionUsers.requireUserId(session);
    }

    /**
     * 프론트엔드(map-detail.js)에서 배열 형태를 직접 기대하더라도, 
     * 규정에 따라 Resp로 감싸서 반환합니다.
     */
    @GetMapping("/rooms")
    public ResponseEntity<Resp<List<BookingResponse.RoomDTO>>> getRooms(
            @RequestParam(name = "lodgingName") String lodgingName,
            @RequestParam(name = "address") String address) {
        List<BookingResponse.RoomDTO> rooms = bookingService.fetchRoomsFromTourApi(this.tourApiServiceKey, lodgingName, address);
        return Resp.ok(rooms);
    }

    /**
     * 프론트엔드 호환성을 위해 DTO를 직접 반환하던 것을 Resp로 감싸도록 수정합니다.
     */
    @GetMapping("/place-image")
    public ResponseEntity<Resp<BookingResponse.PlaceImageDTO>> getPlaceImage(
            @RequestParam(name = "placeUrl", required = false) String placeUrl,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "address", required = false) String address) {
        BookingResponse.PlaceImageDTO image = bookingService.getPlaceImage(this.tourApiServiceKey, placeUrl, name, address);
        return Resp.ok(image);
    }

    /**
     * 프론트엔드(map-detail.js) 호환성을 위해 DTO를 직접 반환하던 것을 Resp로 감싸도록 수정합니다.
     */
    @PostMapping("/map-pois/merge")
    public ResponseEntity<Resp<List<BookingResponse.MapPoiDTO>>> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingResponse.MapPoiDTO> pois = bookingService.mergeMapPois(reqDTO);
        return Resp.ok(pois);
    }
}
