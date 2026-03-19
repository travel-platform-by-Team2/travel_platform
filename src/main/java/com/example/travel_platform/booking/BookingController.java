package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private static final String DEFAULT_COMPLETE_IMAGE_URL =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuC-tNVV57D0EwHVcc8AGgHsqFcUf1oHeJUsCxZ-987Qnye2F7JO9sQyk8t_AWfw0W3RDx8bJWwNKOLLAFJe_IIC1x8Pdg3Q6_YzcyaKkC7GitmYoVQPK24H1H4ZGnJYOn_ihHy2Tp-8xS1yfeVoS0dIPgu3UwUeR3w16rvw0eJ-X49iGCKDq0ku2fbWdoYPv_RklQ4NrLhuBb5HSC1KdxB4_6rQkDx3n2Z8l1IsBQTL0F_C2wv7gApGTmObL4V1gUyPs9A2p3zThbw";

    private final String kakaoMapAppKey;
    private final String tourApiServiceKey;
    private final BookingService bookingService;
    private final HttpSession session;

    public BookingController(
            @Value("${KAKAO_MAP_APP_KEY:}") String kakaoMapAppKey,
            @Value("${TOUR_API_SERVICE_KEY:}") String tourApiServiceKey,
            BookingService bookingService,
            HttpSession session) {
        this.kakaoMapAppKey = kakaoMapAppKey;
        this.tourApiServiceKey = tourApiServiceKey;
        this.bookingService = bookingService;
        this.session = session;
    }

    @GetMapping("/map-detail")
    public String detailMap(Model model) {
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey == null ? "" : kakaoMapAppKey);
        return "pages/map-detail";
    }

    @GetMapping("/checkout")
    public String checkoutPage(
            @RequestParam(name = "lodgingName", required = false, defaultValue = "숙소") String lodgingName,
            @RequestParam(name = "roomName", required = false, defaultValue = "기본 객실") String roomName,
            @RequestParam(name = "address", required = false, defaultValue = "주소 정보 없음") String address,
            @RequestParam(name = "imageUrl", required = false) String imageUrl,
            @RequestParam(name = "checkIn", required = false, defaultValue = "") String checkIn,
            @RequestParam(name = "checkOut", required = false, defaultValue = "") String checkOut,
            @RequestParam(name = "guests", required = false, defaultValue = "성인 2명") String guests,
            @RequestParam(name = "roomPrice", required = false, defaultValue = "350000") Integer roomPrice,
            @RequestParam(name = "fee", required = false, defaultValue = "105000") Integer fee,
            Model model) {

        int safeRoomPrice = roomPrice == null || roomPrice < 0 ? 350000 : roomPrice;
        int safeFee = fee == null || fee < 0 ? 105000 : fee;
        long nights = calculateNights(checkIn, checkOut);
        String nightsLabel = nights + "박";
        long roomSubtotal = (long) safeRoomPrice * nights;
        long feeSubtotal = (long) safeFee * nights;
        long totalPrice = roomSubtotal + feeSubtotal;

        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("roomName", roomName);
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
        model.addAttribute("totalPriceRaw", totalPrice);

        User sessionUser = resolveSessionUser();
        if (sessionUser != null) {
            User user = bookingService.getUserById(sessionUser.getId());
            if (user == null) {
                user = sessionUser;
            }
            model.addAttribute("bookerName", user.getUsername() == null ? "" : user.getUsername());
            model.addAttribute("bookerEmail", user.getEmail() == null ? "" : user.getEmail());
            model.addAttribute("bookerPhone", user.getTel() == null ? "" : user.getTel());
        } else {
            model.addAttribute("bookerName", "");
            model.addAttribute("bookerEmail", "");
            model.addAttribute("bookerPhone", "");
        }
        return "pages/booking-checkout";
    }

    @GetMapping("/complete")
    public String completePage(
            @RequestParam(name = "lodgingName", required = false, defaultValue = "숙소") String lodgingName,
            @RequestParam(name = "roomName", required = false, defaultValue = "기본 객실") String roomName,
            @RequestParam(name = "region", required = false, defaultValue = "") String region,
            @RequestParam(name = "guests", required = false, defaultValue = "성인 2명") String guests,
            @RequestParam(name = "checkIn", required = false, defaultValue = "") String checkIn,
            @RequestParam(name = "checkOut", required = false, defaultValue = "") String checkOut,
            @RequestParam(name = "totalPriceText", required = false, defaultValue = "") String totalPriceText,
            @RequestParam(name = "totalPriceRaw", required = false) Integer totalPriceRaw,
            @RequestParam(name = "imageUrl", required = false) String imageUrl,
            Model model) {

        String safeRegion = (region == null || region.isBlank()) ? "지역 정보 없음" : region;
        String regionKey = normalizeRegionKey(safeRegion);

        User sessionUser = resolveSessionUser();
        if (sessionUser != null) {
            bookingService.processBookingCompletion(
                    sessionUser.getId(),
                    lodgingName + " (" + roomName + ")",
                    regionKey,
                    checkIn,
                    checkOut,
                    guests,
                    totalPriceRaw,
                    imageUrl);
        }

        String safeTotalPriceText = (totalPriceText == null || totalPriceText.isBlank()) ? "0원" : totalPriceText;

        model.addAttribute("bookingNumber", buildBookingNumber());
        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("roomName", roomName);
        model.addAttribute("region", safeRegion);
        model.addAttribute("regionKey", regionKey);
        model.addAttribute("guests", guests);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("nightsLabel", calculateNightsLabel(checkIn, checkOut));
        model.addAttribute("totalPriceText", safeTotalPriceText);
        model.addAttribute("completeImageUrl", resolveCompleteImageUrl(imageUrl));
        return "pages/booking-complete";
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

    private String normalizeRegionKey(String region) {
        String text = region == null ? "" : region.trim();
        if (text.isBlank()) {
            return "busan";
        }

        String lower = text.toLowerCase();
        if (lower.equals("seoul") || lower.equals("busan") || lower.equals("daegu") || lower.equals("incheon")
                || lower.equals("gwangju") || lower.equals("daejeon") || lower.equals("ulsan")
                || lower.equals("sejong") || lower.equals("gyeonggi") || lower.equals("gangwon")
                || lower.equals("chungbuk") || lower.equals("chungnam") || lower.equals("jeonbuk")
                || lower.equals("jeonnam") || lower.equals("gyeongbuk") || lower.equals("gyeongnam")
                || lower.equals("jeju")) {
            return lower;
        }

        if (text.contains("서울")) {
            return "seoul";
        }
        if (text.contains("부산")) {
            return "busan";
        }
        if (text.contains("대구")) {
            return "daegu";
        }
        if (text.contains("인천")) {
            return "incheon";
        }
        if (text.contains("광주")) {
            return "gwangju";
        }
        if (text.contains("대전")) {
            return "daejeon";
        }
        if (text.contains("울산")) {
            return "ulsan";
        }
        if (text.contains("세종")) {
            return "sejong";
        }
        if (text.contains("경기") || text.contains("경기도")) {
            return "gyeonggi";
        }
        if (text.contains("강원") || text.contains("강원도") || text.contains("강원특별자치도")) {
            return "gangwon";
        }
        if (text.contains("충북") || text.contains("충청북도")) {
            return "chungbuk";
        }
        if (text.contains("충남") || text.contains("충청남도")) {
            return "chungnam";
        }
        if (text.contains("전북") || text.contains("전라북도")) {
            return "jeonbuk";
        }
        if (text.contains("전남") || text.contains("전라남도")) {
            return "jeonnam";
        }
        if (text.contains("경북") || text.contains("경상북도")) {
            return "gyeongbuk";
        }
        if (text.contains("경남") || text.contains("경상남도")) {
            return "gyeongnam";
        }
        if (text.contains("제주")) {
            return "jeju";
        }
        return "busan";
    }

    private User resolveSessionUser() {
        Object sessionUser = session.getAttribute("sessionUser");
        if (sessionUser instanceof User user) {
            return user;
        }
        return null;
    }
}
