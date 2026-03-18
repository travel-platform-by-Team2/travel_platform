package com.example.travel_platform.booking;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void processBookingCompletion(
            Integer sessionUserId,
            String lodgingName,
            String regionKey,
            String checkIn,
            String checkOut,
            String guests,
            Integer totalPriceRaw,
            String imageUrl) {

        User user = userRepository.findById(sessionUserId).orElse(null);
        if (user == null) {
            return;
        }

        LocalDate checkInDate = resolveDate(checkIn, LocalDate.now());
        LocalDate checkOutDate = resolveDate(checkOut, LocalDate.now().plusDays(1));

        Booking booking = new Booking();
        booking.setUser(user);

        var plans = tripRepository.findPlanListByUserId(user.getId(), 0, 1);
        TripPlan plan;
        if (!plans.isEmpty()) {
            plan = plans.get(0);
        } else {
            plan = TripPlan.create(
                    user,
                    "나의 여행 계획",
                    blankToDefault(regionKey, "busan"),
                    null,
                    checkInDate,
                    checkOutDate,
                    blankToDefault(imageUrl, ""));
            plan = tripRepository.savePlan(plan);
        }

        booking.setTripPlan(plan);
        booking.setLodgingName(lodgingName);
        booking.setCheckIn(checkInDate);
        booking.setCheckOut(checkOutDate);
        booking.setGuestCount(parseGuestCount(guests));
        booking.setTotalPrice(totalPriceRaw == null ? 0 : totalPriceRaw);
        booking.setImageUrl(imageUrl);

        bookingRepository.save(booking);
    }

    @Transactional
    public void createBooking(Integer sessionUserId, BookingRequest.CreateBookingDTO reqDTO) {
        // TODO: 구현 예정
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
        // TODO: 구현 예정
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        return List.of();
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        return null;
    }

    /**
     * 장소 이름으로 캐싱된 이미지를 찾거나, 없으면 스크래핑 후 캐싱합니다.
     */
    @Transactional
    public Map<String, Object> getPlaceImage(String placeUrl, String name) {
        String normalizedName = normalizeName(name);
        String imageUrl = bookingRepository.findImageUrlByNormalizedName(normalizedName).orElse(null);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = resolveImageFromKakaoPlace(placeUrl);
            if (imageUrl != null && !imageUrl.isBlank() && !normalizedName.isBlank()) {
                bookingRepository.upsertMapPlaceImage(name, normalizedName, imageUrl, "KAKAO_PLACE");
            }
        }

        return Map.of(
                "imageUrl", imageUrl == null ? "" : imageUrl,
                "name", name == null ? "" : name);
    }

    /**
     * 카카오 POI와 DB 내 숙소 목록(DTO 프로젝션 활용)을 통합하여 반환합니다.
     */
    public List<BookingRequest.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingRequest.MapPoiDTO> kakaoPois = reqDTO == null || reqDTO.getKakaoPois() == null
                ? List.of()
                : reqDTO.getKakaoPois();

        String regionKey = reqDTO == null ? "" : blankToDefault(reqDTO.getRegionKey(), "");
        double[] bounds = resolveBounds(reqDTO == null ? null : reqDTO.getBounds());

        // 1. 리포지토리에서 바로 DTO 목록을 받아옵니다.
        List<BookingRequest.MapPoiDTO> dbPois = bookingRepository.findActiveLodgingsInBounds(
                regionKey, bounds[0], bounds[1], bounds[2], bounds[3]);

        // 2. 통합(Merge) 로직 진행
        LinkedHashMap<String, BookingRequest.MapPoiDTO> merged = new LinkedHashMap<>();
        for (BookingRequest.MapPoiDTO item : kakaoPois) {
            BookingRequest.MapPoiDTO normalized = normalizePoi(item, "KAKAO");
            if (normalized != null) {
                merged.put(buildPoiKey(normalized), normalized);
            }
        }
        for (BookingRequest.MapPoiDTO item : dbPois) {
            BookingRequest.MapPoiDTO normalized = normalizePoi(item, "DB");
            if (normalized != null) {
                String key = buildPoiKey(normalized);
                BookingRequest.MapPoiDTO existing = merged.get(key);
                if (existing == null || "KAKAO".equals(existing.getSource())) {
                    merged.put(key, normalized);
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private LocalDate resolveDate(String dateText, LocalDate defaultDate) {
        try {
            if (dateText == null || dateText.isBlank())
                return defaultDate;
            return LocalDate.parse(dateText);
        } catch (Exception e) {
            return defaultDate;
        }
    }

    private int parseGuestCount(String guests) {
        try {
            return Integer.parseInt(guests.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 2;
        }
    }

    private String normalizeName(String name) {
        return (name == null) ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private double[] resolveBounds(BookingRequest.MapBoundsDTO bounds) {
        double minLat = -90.0, maxLat = 90.0, minLng = -180.0, maxLng = 180.0;
        if (bounds == null)
            return new double[] { minLat, maxLat, minLng, maxLng };

        if (isValidCoordinate(bounds.getSwLat()) && isValidCoordinate(bounds.getNeLat())) {
            minLat = Math.min(bounds.getSwLat(), bounds.getNeLat());
            maxLat = Math.max(bounds.getSwLat(), bounds.getNeLat());
        }
        if (isValidCoordinate(bounds.getSwLng()) && isValidCoordinate(bounds.getNeLng())) {
            minLng = Math.min(bounds.getSwLng(), bounds.getNeLng());
            maxLng = Math.max(bounds.getSwLng(), bounds.getNeLng());
        }
        return new double[] { minLat, maxLat, minLng, maxLng };
    }

    private boolean isValidCoordinate(Double value) {
        return value != null && Double.isFinite(value);
    }

    private String blankToDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private BookingRequest.MapPoiDTO normalizePoi(BookingRequest.MapPoiDTO item, String defaultSource) {
        if (item == null || !isValidCoordinate(item.getLat()) || !isValidCoordinate(item.getLng()))
            return null;

        BookingRequest.MapPoiDTO poi = new BookingRequest.MapPoiDTO();
        poi.setExternalPlaceId(blankToDefault(item.getExternalPlaceId(), ""));
        poi.setName(blankToDefault(item.getName(), "숙소"));
        poi.setPhone(blankToDefault(item.getPhone(), ""));
        poi.setAddress(blankToDefault(item.getAddress(), ""));
        poi.setRoadAddress(blankToDefault(item.getRoadAddress(), ""));
        poi.setPlaceUrl(blankToDefault(item.getPlaceUrl(), ""));
        poi.setCategoryName(blankToDefault(item.getCategoryName(), ""));
        poi.setCategoryGroupCode(blankToDefault(item.getCategoryGroupCode(), ""));
        poi.setLat(item.getLat());
        poi.setLng(item.getLng());

        String type = blankToDefault(item.getType(), "");
        if (type.isBlank()) {
            type = "AD5".equalsIgnoreCase(poi.getCategoryGroupCode()) ? "hotel" : "attraction";
        }
        poi.setType(type);
        poi.setSource(blankToDefault(item.getSource(), defaultSource));
        return poi;
    }

    private String buildPoiKey(BookingRequest.MapPoiDTO item) {
        if (item.getExternalPlaceId() != null && !item.getExternalPlaceId().isBlank()) {
            return "id:" + item.getExternalPlaceId();
        }
        String normalized = normalizeName(item.getName());
        String lat = String.format("%.4f", item.getLat());
        String lng = String.format("%.4f", item.getLng());
        return "geo:" + normalized + ":" + lat + ":" + lng;
    }

    private String resolveImageFromKakaoPlace(String placeUrl) {
        if (placeUrl == null || placeUrl.isBlank() || !isAllowedKakaoUrl(placeUrl))
            return null;
        try {
            Document doc = Jsoup.connect(placeUrl)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                    .timeout((int) Duration.ofSeconds(4).toMillis())
                    .get();

            String image = doc.select("meta[property=og:image]").attr("content");
            if (image.isBlank())
                image = doc.select("meta[name=twitter:image]").attr("content");
            if (image.isBlank()) {
                image = doc.select("img[src]").stream()
                        .map(el -> el.attr("abs:src"))
                        .filter(src -> !src.isBlank())
                        .findFirst()
                        .orElse("");
            }
            return image.isBlank() ? null : image;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAllowedKakaoUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            return host != null && (host.endsWith("map.kakao.com") || host.endsWith("place.map.kakao.com"));
        } catch (Exception e) {
            return false;
        }
    }
}
