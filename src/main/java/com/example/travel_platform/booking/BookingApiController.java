package com.example.travel_platform.booking;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingApiController {

    private final BookingService bookingService;

    @PostMapping
    public void createBooking(@Valid @RequestBody BookingRequest.CreateBookingDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        bookingService.createBooking(1, reqDTO);
    }

    @DeleteMapping("/{bookingId}")
    public void cancelBooking(@PathVariable Integer bookingId) {
        // TODO: 세션 사용자 식별값 연동
        bookingService.cancelBooking(1, bookingId);
    }

    @GetMapping
    public Object getBookingList() {
        // TODO: 조회 파라미터(기간/상태) 확정
        return bookingService.getBookingList(1);
    }

    @GetMapping("/{bookingId}")
    public Object getBookingDetail(@PathVariable Integer bookingId) {
        // TODO: 응답 스펙 확정
        return bookingService.getBookingDetail(1, bookingId);
    }

    @GetMapping("/place-image")
    public Map<String, Object> getPlaceImage(@RequestParam(required = false) String placeUrl,
            @RequestParam(required = false) String name) {
        return bookingService.getPlaceImage(placeUrl, name);
    }

    @PostMapping("/map-pois/merge")
    public Map<String, Object> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        return Map.of("items", bookingService.mergeMapPois(reqDTO));
    }
}
