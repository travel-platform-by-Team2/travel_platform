package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private static final String DEFAULT_COMPLETE_IMAGE_URL =
            "https://lh3.googleusercontent.com/aida-public/AB6AXuC-tNVV57D0EwHVcc8AGgHsqFcUf1oHeJUsCxZ-987Qnye2F7JO9sQyk8t_AWfw0W3RDx8bJWwNKOLLAFJe_IIC1x8Pdg3Q6_YzcyaKkC7GitmYoVQPK24H1H4ZGnJYOn_ihHy2Tp-8xS1yfeVoS0dIPgu3UwUeR3w16rvw0eJ-X49iGCKDq0ku2fbWdoYPv_RklQ4NrLhuBb5HSC1KdxB4_6rQkDx3n2Z8l1IsBQTL0F_C2wv7gApGTmObL4V1gUyPs9A2p3zThbw";
    private static final String MAP_DETAIL_VIEW = "pages/map-detail";
    private static final String CHECKOUT_VIEW = "pages/booking-checkout";
    private static final String COMPLETE_VIEW = "pages/booking-complete";

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
        return renderMapDetailPage(model);
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
        return renderCheckoutPage(
                model,
                buildCheckoutPage(
                        lodgingName,
                        roomName,
                        address,
                        imageUrl,
                        checkIn,
                        checkOut,
                        guests,
                        roomPrice,
                        fee));
    }

    @GetMapping("/complete")
    public String completePage(
            @RequestParam(name = "lodgingName", required = false, defaultValue = "숙소") String lodgingName,
            @RequestParam(name = "roomName", required = false, defaultValue = "기본 객실") String roomName,
            @RequestParam(name = "region", required = false, defaultValue = "") String region,
            @RequestParam(name = "regionKey", required = false, defaultValue = "") String regionKey,
            @RequestParam(name = "guests", required = false, defaultValue = "성인 2명") String guests,
            @RequestParam(name = "checkIn", required = false, defaultValue = "") String checkIn,
            @RequestParam(name = "checkOut", required = false, defaultValue = "") String checkOut,
            @RequestParam(name = "totalPriceText", required = false, defaultValue = "") String totalPriceText,
            @RequestParam(name = "pricePerNight", required = false, defaultValue = "0") Integer pricePerNight,
            @RequestParam(name = "totalFee", required = false, defaultValue = "0") Integer totalFee,
            @RequestParam(name = "imageUrl", required = false) String imageUrl,
            Model model) {
        String safeRegion = (region == null || region.isBlank()) ? "지역 정보 없음" : region;
        String safeRegionKey = (regionKey == null || regionKey.isBlank()) ? normalizeRegionKey(safeRegion) : normalizeRegionKey(regionKey);
        String locationName = toLocationName(safeRegionKey);

        SessionUser sessionUser = resolveSessionUser();
        if (sessionUser != null) {
            bookingService.processBookingCompletion(
                    sessionUser.getId(),
                    BookingRequest.CompleteBookingDTO.builder()
                            .lodgingName(lodgingName)
                            .roomName(roomName)
                            .regionKey(safeRegionKey)
                            .location(locationName)
                            .checkIn(checkIn)
                            .checkOut(checkOut)
                            .guests(guests)
                            .pricePerNight(pricePerNight)
                            .taxAndServiceFee(totalFee)
                            .imageUrl(imageUrl)
                            .build());
        }
        return renderCompletePage(
                model,
                buildCompletePage(
                        lodgingName,
                        roomName,
                        locationName,
                        safeRegionKey,
                        guests,
                        checkIn,
                        checkOut,
                        totalPriceText,
                        imageUrl));
    }

    private String calculateNightsLabel(String checkIn, String checkOut) {
        return calculateNights(checkIn, checkOut) + "박";
    }

    private long calculateNights(String checkIn, String checkOut) {
        if (checkIn == null || checkOut == null || checkIn.isBlank() || checkOut.isBlank()) {
            return 1L;
        }

        try {
            LocalDate in = LocalDate.parse(checkIn);
            LocalDate out = LocalDate.parse(checkOut);
            return Math.max(1, ChronoUnit.DAYS.between(in, out));
        } catch (DateTimeParseException e) {
            throw new Exception400("숙박 날짜 형식이 올바르지 않습니다.");
        }
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

    private SessionUser resolveSessionUser() {
        return SessionUsers.getOrNull(session);
    }

    private String toLocationName(String regionKey) {
        return switch (regionKey) {
            case "seoul" -> "서울";
            case "busan" -> "부산";
            case "daegu" -> "대구";
            case "incheon" -> "인천";
            case "gwangju" -> "광주";
            case "daejeon" -> "대전";
            case "ulsan" -> "울산";
            case "sejong" -> "세종";
            case "gyeonggi" -> "경기";
            case "gangwon" -> "강원";
            case "chungbuk" -> "충북";
            case "chungnam" -> "충남";
            case "jeonbuk" -> "전북";
            case "jeonnam" -> "전남";
            case "gyeongbuk" -> "경북";
            case "gyeongnam" -> "경남";
            case "jeju" -> "제주";
            default -> "부산";
        };
    }

    private String renderMapDetailPage(Model model) {
        model.addAttribute("model", BookingResponse.MapDetailPageDTO.createMapDetailPage(kakaoMapAppKey));
        return MAP_DETAIL_VIEW;
    }

    private String renderCheckoutPage(Model model, BookingResponse.CheckoutPageDTO page) {
        model.addAttribute("model", page);
        return CHECKOUT_VIEW;
    }

    private String renderCompletePage(Model model, BookingResponse.CompletePageDTO page) {
        model.addAttribute("model", page);
        return COMPLETE_VIEW;
    }

    private BookingResponse.CheckoutPageDTO buildCheckoutPage(
            String lodgingName,
            String roomName,
            String address,
            String imageUrl,
            String checkIn,
            String checkOut,
            String guests,
            Integer roomPrice,
            Integer fee) {
            int safeRoomPrice = resolveRoomPrice(roomPrice);
        int safeFee = resolveFee(fee);
        long nights = calculateNights(checkIn, checkOut);
        long roomSubtotal = (long) safeRoomPrice * nights;
        long feeSubtotal = (long) safeFee * nights;
        long totalPrice = roomSubtotal + feeSubtotal;
        String regionKey = normalizeRegionKey(address);
        String regionLabel = toLocationName(regionKey);
        User booker = resolveBooker();
        SessionUser sessionUser = resolveSessionUser();

        return BookingResponse.CheckoutPageDTO.createCheckoutPage(
                lodgingName,
                roomName,
                address,
                regionKey,
                regionLabel,
                imageUrl,
                checkIn,
                checkOut,
                nights + "박",
                guests,
                safeRoomPrice,
                roomSubtotal,
                feeSubtotal,
                totalPrice,
                resolveBookerName(booker, sessionUser),
                resolveBookerEmail(booker, sessionUser),
                resolveBookerPhone(booker, sessionUser));
    }

    private BookingResponse.CompletePageDTO buildCompletePage(
            String lodgingName,
            String roomName,
            String region,
            String regionKey,
            String guests,
            String checkIn,
            String checkOut,
            String totalPriceText,
            String imageUrl) {
        return BookingResponse.CompletePageDTO.createCompletePage(
                buildBookingNumber(),
                lodgingName,
                roomName,
                region,
                regionKey,
                guests,
                checkIn,
                checkOut,
                calculateNightsLabel(checkIn, checkOut),
                totalPriceText,
                resolveCompleteImageUrl(imageUrl));
    }

    private int resolveRoomPrice(Integer roomPrice) {
        if (roomPrice == null || roomPrice < 0) {
            return 350000;
        }
        return roomPrice;
    }

    private int resolveFee(Integer fee) {
        if (fee == null || fee < 0) {
            return 105000;
        }
        return fee;
    }

    private User resolveBooker() {
        SessionUser sessionUser = resolveSessionUser();
        if (sessionUser == null) {
            return null;
        }
        return bookingService.getUserById(sessionUser.getId());
    }

    private String resolveBookerName(User booker, SessionUser sessionUser) {
        if (booker != null) {
            return blankToEmpty(booker.getUsername());
        }
        if (sessionUser != null) {
            return blankToEmpty(sessionUser.getUsername());
        }
        return "";
    }

    private String resolveBookerEmail(User booker, SessionUser sessionUser) {
        if (booker != null) {
            return blankToEmpty(booker.getEmail());
        }
        if (sessionUser != null) {
            return blankToEmpty(sessionUser.getEmail());
        }
        return "";
    }

    private String resolveBookerPhone(User booker, SessionUser sessionUser) {
        if (booker != null) {
            return blankToEmpty(booker.getTel());
        }
        if (sessionUser != null) {
            return blankToEmpty(sessionUser.getTel());
        }
        return "";
    }

    private String blankToEmpty(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value;
    }
}
