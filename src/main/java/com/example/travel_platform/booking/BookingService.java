package com.example.travel_platform.booking;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MapPlaceImageRepository mapPlaceImageRepository;
    private final LodgingQueryRepository lodgingQueryRepository;

    @Transactional
    public void createBooking(Integer sessionUserId, BookingRequest.CreateBookingDTO reqDTO) {
        // TODO: 예약 가능 여부 검증(중복 날짜 등)
        // TODO: 금액/인원 유효성 검증
        // TODO: 엔티티 변환 및 저장
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
        // TODO: 소유권 검증
        // TODO: 취소 정책 반영
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        // TODO: 사용자 예약 목록 조회
        // TODO: BookingSummaryDTO 매핑
        return List.of();
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        // TODO: 상세 조회 + 소유권 검증
        // TODO: BookingDetailDTO 매핑
        return null;
    }

    public Map<String, Object> getPlaceImage(String placeUrl, String name) {
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

    public List<BookingRequest.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingRequest.MapPoiDTO> kakaoPois = reqDTO == null || reqDTO.getKakaoPois() == null
                ? List.of()
                : reqDTO.getKakaoPois();

        String regionKey = reqDTO == null ? "" : blankToDefault(reqDTO.getRegionKey(), "");
        double[] bounds = resolveBounds(reqDTO == null ? null : reqDTO.getBounds());
        List<BookingRequest.MapPoiDTO> dbPois = lodgingQueryRepository.findActiveLodgingsInBounds(
                regionKey,
                bounds[0],
                bounds[1],
                bounds[2],
                bounds[3]);

        LinkedHashMap<String, BookingRequest.MapPoiDTO> merged = new LinkedHashMap<>();
        for (BookingRequest.MapPoiDTO item : kakaoPois) {
            BookingRequest.MapPoiDTO normalized = normalizePoi(item, "KAKAO");
            if (normalized == null) {
                continue;
            }
            merged.put(buildPoiKey(normalized), normalized);
        }
        for (BookingRequest.MapPoiDTO item : dbPois) {
            BookingRequest.MapPoiDTO normalized = normalizePoi(item, "DB");
            if (normalized == null) {
                continue;
            }
            String key = buildPoiKey(normalized);
            BookingRequest.MapPoiDTO existing = merged.get(key);
            if (existing == null || "KAKAO".equals(existing.getSource())) {
                merged.put(key, normalized);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    private double[] resolveBounds(BookingRequest.MapBoundsDTO bounds) {
        double minLat = -90.0;
        double maxLat = 90.0;
        double minLng = -180.0;
        double maxLng = 180.0;
        if (bounds == null) {
            return new double[] { minLat, maxLat, minLng, maxLng };
        }
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
        if (item == null || !isValidCoordinate(item.getLat()) || !isValidCoordinate(item.getLng())) {
            return null;
        }
        BookingRequest.MapPoiDTO poi = new BookingRequest.MapPoiDTO();
        poi.setExternalPlaceId(blankToDefault(item.getExternalPlaceId(), ""));
        poi.setName(blankToDefault(item.getName(), "장소"));
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

