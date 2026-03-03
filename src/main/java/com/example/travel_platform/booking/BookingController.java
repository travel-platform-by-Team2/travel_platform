package com.example.travel_platform.booking;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    private final BookingService bookingService;
    private final MapPlaceImageRepository mapPlaceImageRepository;

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
        int totalPrice = safeRoomPrice + safeFee;
        String nightsLabel = "1박";

        try {
            if (!checkIn.isBlank() && !checkOut.isBlank()) {
                LocalDate in = LocalDate.parse(checkIn);
                LocalDate out = LocalDate.parse(checkOut);
                long nights = Math.max(1, ChronoUnit.DAYS.between(in, out));
                nightsLabel = nights + "박";
            }
        } catch (Exception ignored) {
        }

        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("address", address);
        model.addAttribute("imageUrl", imageUrl == null || imageUrl.isBlank() ? "" : imageUrl);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("nightsLabel", nightsLabel);
        model.addAttribute("guests", guests);
        model.addAttribute("roomPriceText", String.format("%,d원", safeRoomPrice));
        model.addAttribute("feeText", String.format("%,d원", safeFee));
        model.addAttribute("totalPriceText", String.format("%,d원", totalPrice));
        return "pages/booking-checkout";
    }

    @GetMapping("/place-image")
    @ResponseBody
    public Map<String, Object> getPlaceImage(
            @RequestParam(required = false) String placeUrl,
            @RequestParam(required = false) String name) {
        String normalizedName = normalizeName(name);
        String imageUrl = mapPlaceImageRepository.findImageUrlByNormalizedName(normalizedName).orElse(null);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = resolveImageFromKakaoPlace(placeUrl);
            if (imageUrl != null && !imageUrl.isBlank() && !normalizedName.isBlank()) {
                mapPlaceImageRepository.upsert(name, normalizedName, imageUrl, "KAKAO_PLACE");
            }
        }

        return Map.of(
                "imageUrl", imageUrl == null ? "" : imageUrl,
                "name", name == null ? "" : name);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    private String resolveImageFromKakaoPlace(String placeUrl) {
        if (placeUrl == null || placeUrl.isBlank() || !isAllowedKakaoUrl(placeUrl)) {
            return null;
        }

        try {
            Document doc = Jsoup.connect(placeUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                    .timeout((int) Duration.ofSeconds(4).toMillis())
                    .followRedirects(true)
                    .get();

            String image = doc.select("meta[property=og:image]").attr("content");
            if (image == null || image.isBlank()) {
                image = doc.select("meta[name=twitter:image]").attr("content");
            }
            if (image == null || image.isBlank()) {
                image = doc.select("img[src]").stream()
                        .map(el -> el.attr("abs:src"))
                        .filter(src -> src != null && !src.isBlank())
                        .findFirst()
                        .orElse("");
            }

            return image == null || image.isBlank() ? null : image;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAllowedKakaoUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            return host.endsWith("map.kakao.com") || host.endsWith("place.map.kakao.com");
        } catch (Exception e) {
            return false;
        }
    }
}
