package com.example.travel_platform.booking;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public void createBooking(@RequestBody BookingRequest.CreateBookingDTO reqDTO) {
        bookingService.createBooking(1, reqDTO);
    }

    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable("bookingId") Integer bookingId) {
        bookingService.cancelBooking(1, bookingId);
    }

    @GetMapping
    public Object getBookingList() {
        return bookingService.getBookingList(1);
    }

    @GetMapping("/{bookingId}")
    public Object getBookingDetail(@PathVariable("bookingId") Integer bookingId) {
        return bookingService.getBookingDetail(1, bookingId);
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getRooms(
            @RequestParam(name = "lodgingName") String lodgingName,
            @RequestParam(name = "address") String address) {
        return bookingService.fetchRoomsFromTourApi(this.tourApiServiceKey, lodgingName, address);
    }

    @GetMapping("/place-image")
    public Map<String, Object> getPlaceImage(
            @RequestParam(value = "placeUrl", required = false) String placeUrl,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address) {
        return bookingService.getPlaceImage(this.tourApiServiceKey, placeUrl, name, address);
    }

    @PostMapping("/map-pois/merge")
    public Map<String, Object> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        return Map.of("items", bookingService.mergeMapPois(reqDTO));
    }
}
