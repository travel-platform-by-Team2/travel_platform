package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String DEFAULT_COMPLETE_IMAGE_URL =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuC-tNVV57D0EwHVcc8AGgHsqFcUf1oHeJUsCxZ-987Qnye2F7JO9sQyk8t_AWfw0W3RDx8bJWwNKOLLAFJe_IIC1x8Pdg3Q6_YzcyaKkC7GitmYoVQPK24H1H4ZGnJYOn_ihHy2Tp-8xS1yfeVoS0dIPgu3UwUeR3w16rvw0eJ-X49iGCKDq0ku2fbWdoYPv_RklQ4NrLhuBb5HSC1KdxB4_6rQkDx3n2Z8l1IsBQTL0F_C2wv7gApGTmObL4V1gUyPs9A2p3zThbw";

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

    @GetMapping("/map-detail")
    public String detailMap() {
        return "pages/map-detail";
    }

    @GetMapping("/checkout")
    public String checkoutPage(
            @RequestParam(required = false, defaultValue = "숙소") String lodgingName,
            @RequestParam(required = false, defaultValue = "주소 정보 없음") String address,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false, defaultValue = "") String checkIn,
            @RequestParam(required = false, defaultValue = "") String checkOut,
            @RequestParam(required = false, defaultValue = "성인 2명") String guests,
            @RequestParam(required = false, defaultValue = "350000") Integer roomPrice,
            @RequestParam(required = false, defaultValue = "105000") Integer fee,
            Model model) {

        int safeRoomPrice = roomPrice == null || roomPrice < 0 ? 350000 : roomPrice;
        int safeFee = fee == null || fee < 0 ? 105000 : fee;
        long nights = calculateNights(checkIn, checkOut);
        String nightsLabel = nights + "박";
        long roomSubtotal = (long) safeRoomPrice * nights;
        long feeSubtotal = (long) safeFee * nights;
        long totalPrice = roomSubtotal + feeSubtotal;

        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("address", address);
        model.addAttribute("imageUrl", imageUrl == null || imageUrl.isBlank() ? "" : imageUrl);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("nightsLabel", nightsLabel);
        model.addAttribute("guests", guests);
        model.addAttribute("roomPriceText", String.format("%,d원", safeRoomPrice));
        model.addAttribute("roomSubtotalText", String.format("%,d원", roomSubtotal));
        model.addAttribute("feeText", String.format("%,d원", feeSubtotal));
        model.addAttribute("totalPriceText", String.format("%,d원", totalPrice));
        return "pages/booking-checkout";
    }

    @GetMapping("/complete")
    public String completePage(
            @RequestParam(required = false, defaultValue = "숙소") String lodgingName,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "성인 2명") String guests,
            @RequestParam(required = false, defaultValue = "") String checkIn,
            @RequestParam(required = false, defaultValue = "") String checkOut,
            @RequestParam(required = false, defaultValue = "") String totalPriceText,
            @RequestParam(required = false) String imageUrl,
            Model model) {

        String safeRegion = (region == null || region.isBlank()) ? "지역 정보 없음" : region;
        String safeTotalPriceText = (totalPriceText == null || totalPriceText.isBlank()) ? "0원" : totalPriceText;

        model.addAttribute("bookingNumber", buildBookingNumber());
        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("region", safeRegion);
        model.addAttribute("guests", guests);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("nightsLabel", calculateNightsLabel(checkIn, checkOut));
        model.addAttribute("totalPriceText", safeTotalPriceText);
        model.addAttribute("completeImageUrl", resolveCompleteImageUrl(imageUrl));
        return "pages/booking-complete";
    }

    @GetMapping("/place-image")
    @ResponseBody
    public Map<String, Object> getPlaceImage(
            @RequestParam(required = false) String placeUrl,
            @RequestParam(required = false) String name) {
        return bookingService.getPlaceImage(placeUrl, name);
    }

    @PostMapping("/map-pois/merge")
    @ResponseBody
    public Map<String, Object> mergeMapPois(@RequestBody BookingRequest.MergeMapPoisDTO reqDTO) {
        return Map.of("items", bookingService.mergeMapPois(reqDTO));
    }

    private String calculateNightsLabel(String checkIn, String checkOut) {
        return calculateNights(checkIn, checkOut) + "박";
    }

    private long calculateNights(String checkIn, String checkOut) {
        try {
            if (checkIn != null && checkOut != null && !checkIn.isBlank() && !checkOut.isBlank()) {
                LocalDate in = LocalDate.parse(checkIn);
                LocalDate out = LocalDate.parse(checkOut);
                return Math.max(1, ChronoUnit.DAYS.between(in, out));
            }
        } catch (Exception ignored) {
        }
        return 1L;
    }

    private String buildBookingNumber() {
        LocalDate now = LocalDate.now();
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return String.format("KR-%04d%02d%02d-%03d", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), random);
    }

    private String resolveCompleteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return DEFAULT_COMPLETE_IMAGE_URL;
        }
        return imageUrl;
    }
}
