package com.example.travel_platform.booking;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;
import tools.jackson.databind.ObjectMapper;

@Transactional(readOnly = true)
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MapPlaceImageRepository mapPlaceImageRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ObjectMapper objectMapper;

    // 명시적 생성자 주입
    public BookingService(
            BookingRepository bookingRepository,
            MapPlaceImageRepository mapPlaceImageRepository,
            UserRepository userRepository,
            TripRepository tripRepository) {
        this.bookingRepository = bookingRepository;
        this.mapPlaceImageRepository = mapPlaceImageRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.objectMapper = new ObjectMapper();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 공통 HTTP 요청 메서드 (AirApp 스타일)
     */
    private String executeGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            StringBuilder json = new StringBuilder();
            try (Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                while (sc.hasNextLine()) {
                    json.append(sc.nextLine());
                }
            }
            return json.toString();
        } catch (Exception e) {
            System.err.println("[TourAPI] 통신 에러: " + e.getMessage());
            return null;
        }
    }

    /**
     * TourAPI 4.0을 사용하여 숙소의 객실 정보를 가져옴.
     */
    public List<Map<String, Object>> fetchRoomsFromTourApi(String serviceKey, String lodgingName, String address) {
        try {
            String contentId = searchTourApiContentId(serviceKey, lodgingName, address);
            if (contentId == null)
                return List.of();

            return fetchTourApiRoomDetails(serviceKey, contentId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private String searchTourApiContentId(String serviceKey, String name, String address) {
        if (serviceKey == null || serviceKey.isBlank())
            return null;
        try {
            String encodedKeyword = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2"
                    + "?serviceKey=" + serviceKey
                    + "&MobileOS=ETC&MobileApp=TravelApp&_type=json"
                    + "&keyword=" + encodedKeyword
                    + "&contentTypeId=32";

            String json = executeGet(urlStr);
            if (json == null)
                return null;

            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> res = (Map<String, Object>) response.get("response");
            if (res == null)
                return null;
            Map<String, Object> body = (Map<String, Object>) res.get("body");
            if (body == null || body.get("items") == null)
                return null;

            Object itemsObj = body.get("items");
            if (itemsObj instanceof String && ((String) itemsObj).isBlank())
                return null;
            if (!(itemsObj instanceof Map))
                return null;

            Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
            Object itemData = itemsMap.get("item");

            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) {
                itemList = (List<Map<String, Object>>) itemData;
            } else if (itemData instanceof Map) {
                itemList.add((Map<String, Object>) itemData);
            }

            if (!itemList.isEmpty()) {
                String simpleInputName = simplifyName(name);
                for (Map<String, Object> item : itemList) {
                    String itemSimpleName = simplifyName((String) item.get("title"));
                    if (itemSimpleName.contains(simpleInputName) || simpleInputName.contains(itemSimpleName)) {
                        return (String) item.get("contentid");
                    }
                }
                return (String) itemList.get(0).get("contentid");
            }
        } catch (Exception e) {
            System.err.println("[TourAPI] ID 검색 실패: " + e.getMessage());
        }
        return null;
    }

    private String simplifyName(String name) {
        if (name == null)
            return "";
        return name.replaceAll("\\s+", "")
                .replaceAll("(호텔|리조트|펜션|모텔|게스트하우스|스테이|민박|여관|해수욕장|공원)", "")
                .replaceAll("[^a-zA-Z0-9가-힣]", "")
                .toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchTourApiRoomDetails(String serviceKey, String contentId) {
        List<Map<String, Object>> roomList = new ArrayList<>();
        try {
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/detailInfo2"
                    + "?serviceKey=" + serviceKey
                    + "&MobileOS=ETC&MobileApp=TravelApp&_type=json"
                    + "&contentId=" + contentId
                    + "&contentTypeId=32";

            String json = executeGet(urlStr);
            if (json == null)
                return roomList;

            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> res = (Map<String, Object>) response.get("response");
            Map<String, Object> body = (Map<String, Object>) res.get("body");
            if (body == null || body.get("items") == null || body.get("items").equals(""))
                return roomList;

            Map<String, Object> itemsObj = (Map<String, Object>) body.get("items");
            Object itemData = itemsObj.get("item");

            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) {
                itemList = (List<Map<String, Object>>) itemData;
            } else if (itemData instanceof Map) {
                itemList.add((Map<String, Object>) itemData);
            }

            for (Map<String, Object> item : itemList) {
                String roomTitle = (String) item.get("roomtitle");
                if (roomTitle == null || roomTitle.isBlank())
                    continue;

                Map<String, Object> room = new LinkedHashMap<>();
                room.put("name", roomTitle);
                room.put("content", item.get("roomintro"));
                room.put("baseCount", item.get("roombasecount"));
                room.put("maxCount", item.get("roommaxcount"));
                room.put("imageUrl", item.get("roomimg1"));
                roomList.add(room);
            }
        } catch (Exception e) {
            System.err.println("[TourAPI] 객실 상세 조회 실패: " + e.getMessage());
        }
        return roomList;
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
        if (user == null)
            return;

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
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        return List.of();
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        return null;
    }

    @Transactional
    public Map<String, Object> getPlaceImage(String serviceKey, String placeUrl, String name, String address) {
        String normalizedName = (name == null) ? "" : name.replaceAll("\\s+", "").toLowerCase();
        String imageUrl = bookingRepository.findImageUrlByNormalizedName(normalizedName).orElse(null);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = fetchImageFromTourApi(serviceKey, name, address);

            if (imageUrl == null || imageUrl.isBlank()) {
                imageUrl = resolveImageFromKakaoPlace(placeUrl);
            }

            if (imageUrl != null && !imageUrl.isBlank() && !normalizedName.isBlank()) {
                String finalImageUrl = imageUrl;
                MapPlaceImage mapPlaceImage = mapPlaceImageRepository.findByNormalizedName(normalizedName)
                        .orElseGet(() -> {
                            MapPlaceImage mi = new MapPlaceImage();
                            mi.setNormalizedName(normalizedName);
                            return mi;
                        });

                mapPlaceImage.setPlaceName(name != null ? name : normalizedName);
                mapPlaceImage.setImageUrl(finalImageUrl);
                mapPlaceImage.setSource("TOUR_API_OR_KAKAO");

                mapPlaceImageRepository.save(mapPlaceImage);
            }
        }

        return Map.of(
                "imageUrl", imageUrl == null ? "" : imageUrl,
                "name", name == null ? "" : name);
    }

    @SuppressWarnings("unchecked")
    private String fetchImageFromTourApi(String serviceKey, String name, String address) {
        if (serviceKey == null || serviceKey.isBlank() || name == null || name.isBlank())
            return null;
        try {
            String encodedKeyword = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2"
                    + "?serviceKey=" + serviceKey
                    + "&MobileOS=ETC&MobileApp=TravelApp&_type=json"
                    + "&keyword=" + encodedKeyword;

            String json = executeGet(urlStr);
            if (json == null)
                return null;

            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> res = (Map<String, Object>) response.get("response");
            Map<String, Object> body = (Map<String, Object>) res.get("body");
            Map<String, Object> itemsObj = (Map<String, Object>) body.get("items");

            if (itemsObj != null && itemsObj.get("item") instanceof List) {
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsObj.get("item");

                String targetAddr = (address != null) ? address.substring(0, Math.min(address.length(), 2)) : "";
                for (Map<String, Object> item : itemList) {
                    String firstImage = (String) item.get("firstimage");
                    String itemAddr = (String) item.get("addr1");
                    if (firstImage != null && !firstImage.isBlank()) {
                        if (targetAddr.isEmpty() || (itemAddr != null && itemAddr.contains(targetAddr))) {
                            return firstImage;
                        }
                    }
                }
                for (Map<String, Object> item : itemList) {
                    String firstImage = (String) item.get("firstimage");
                    if (firstImage != null && !firstImage.isBlank())
                        return firstImage;
                }
            }
        } catch (Exception e) {
            System.err.println("[TourAPI] 이미지 조회 실패: " + e.getMessage());
        }
        return null;
    }

    public List<BookingRequest.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingRequest.MapPoiDTO> kakaoPois = reqDTO == null || reqDTO.getKakaoPois() == null ? List.of()
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
            if (normalized != null)
                merged.put(buildPoiKey(normalized), normalized);
        }
        for (BookingRequest.MapPoiDTO item : dbPois) {
            BookingRequest.MapPoiDTO normalized = normalizePoi(item, "DB");
            if (normalized != null) {
                String key = buildPoiKey(normalized);
                BookingRequest.MapPoiDTO existing = merged.get(key);
                if (existing == null || "KAKAO".equals(existing.getSource()))
                    merged.put(key, normalized);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private LocalDate resolveDate(String dateText, LocalDate defaultDate) {
        try {
            return (dateText == null || dateText.isBlank()) ? defaultDate : LocalDate.parse(dateText);
        } catch (Exception e) {
            return defaultDate;
        }
    }

    private int parseGuestCount(String guests) {
        try {
            return Integer.parseInt(guests.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return BookVar.DEFAULT_PERSON_COUNT;
        }
    }

    private double[] resolveBounds(BookingRequest.MapBoundsDTO bounds) {
        double minLat = -90.0, maxLat = 90.0, minLng = -180.0, maxLng = 180.0;
        if (bounds != null) {
            if (isValidCoordinate(bounds.getSwLat()) && isValidCoordinate(bounds.getNeLat())) {
                minLat = Math.min(bounds.getSwLat(), bounds.getNeLat());
                maxLat = Math.max(bounds.getSwLat(), bounds.getNeLat());
            }
            if (isValidCoordinate(bounds.getSwLng()) && isValidCoordinate(bounds.getNeLng())) {
                minLng = Math.min(bounds.getSwLng(), bounds.getNeLng());
                maxLng = Math.max(bounds.getSwLng(), bounds.getNeLng());
            }
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
        if (type.isBlank())
            type = "AD5".equalsIgnoreCase(poi.getCategoryGroupCode()) ? "hotel" : "attraction";
        poi.setType(type);
        poi.setSource(blankToDefault(item.getSource(), defaultSource));
        return poi;
    }

    private String buildPoiKey(BookingRequest.MapPoiDTO item) {
        if (item.getExternalPlaceId() != null && !item.getExternalPlaceId().isBlank())
            return "id:" + item.getExternalPlaceId();
        String normalized = (item.getName() == null) ? "" : item.getName().replaceAll("\\s+", "").toLowerCase();
        String lat = String.format("%.4f", item.getLat());
        String lng = String.format("%.4f", item.getLng());
        return "geo:" + normalized + ":" + lat + ":" + lng;
    }

    private String resolveImageFromKakaoPlace(String placeUrl) {
        if (placeUrl == null || placeUrl.isBlank() || !isAllowedKakaoUrl(placeUrl))
            return null;
        try {
            Document doc = Jsoup.connect(placeUrl).userAgent("Mozilla/5.0").timeout(4000).get();
            String image = doc.select("meta[property=og:image]").attr("content");
            if (image.isBlank())
                image = doc.select("meta[name=twitter:image]").attr("content");
            return image.isBlank() ? null : image;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAllowedKakaoUrl(String rawUrl) {
        try {
            String host = java.net.URI.create(rawUrl).getHost();
            return host != null && (host.endsWith("map.kakao.com") || host.endsWith("place.map.kakao.com"));
        } catch (Exception e) {
            return false;
        }
    }
}
