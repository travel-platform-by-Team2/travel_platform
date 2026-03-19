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

import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;
import tools.jackson.databind.ObjectMapper;

@Transactional(readOnly = true)
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ObjectMapper objectMapper;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, TripRepository tripRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.objectMapper = new ObjectMapper();
    }

    public User getUserById(Integer id) { return userRepository.findById(id).orElse(null); }

    private String executeGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() != 200) return null;
            StringBuilder json = new StringBuilder();
            try (Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                while (sc.hasNextLine()) json.append(sc.nextLine());
            }
            return json.toString();
        } catch (Exception e) { return null; }
    }

    public List<BookingResponse.RoomDTO> fetchRoomsFromTourApi(String serviceKey, String lodgingName, String address) {
        try {
            String contentId = searchTourApiContentId(serviceKey, lodgingName, address);
            if (contentId == null) return List.of();
            List<BookingResponse.RoomDTO> rooms = fetchTourApiRoomDetails(serviceKey, contentId);
            List<String> extraImages = fetchTourApiAdditionalImages(serviceKey, contentId);
            if (rooms.isEmpty() && !extraImages.isEmpty()) {
                List<BookingResponse.RoomDTO> fallback = new ArrayList<>();
                fallback.add(BookingResponse.RoomDTO.builder().name("기본 객실").content("상세 정보는 숙소에 문의해 주세요.").imageUrl(extraImages.get(0)).allImages(extraImages).build());
                return fallback;
            }
            for (BookingResponse.RoomDTO room : rooms) room.setAllImages(extraImages);
            return rooms;
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    private String searchTourApiContentId(String serviceKey, String name, String address) {
        try {
            String encodedKeyword = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=TravelApp&_type=json&keyword=" + encodedKeyword;
            String json = executeGet(urlStr); if (json == null) return null;
            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) response.get("response")).get("body");
            Object itemsObj = body.get("items"); if (itemsObj == null || itemsObj instanceof String) return null;
            Object itemData = ((Map<String, Object>) itemsObj).get("item");
            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) itemList = (List<Map<String, Object>>) itemData;
            else if (itemData instanceof Map) itemList.add((Map<String, Object>) itemData);
            if (!itemList.isEmpty()) {
                String simpleInputName = simplifyName(name);
                for (Map<String, Object> item : itemList) {
                    String title = (String) item.get("title");
                    if (title != null && simplifyName(title).contains(simpleInputName)) return (String) item.get("contentid");
                }
                return (String) itemList.get(0).get("contentid");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<BookingResponse.RoomDTO> fetchTourApiRoomDetails(String serviceKey, String contentId) {
        List<BookingResponse.RoomDTO> roomList = new ArrayList<>();
        try {
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/detailInfo2?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=TravelApp&_type=json&contentId=" + contentId + "&contentTypeId=32";
            String json = executeGet(urlStr); if (json == null) return roomList;
            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) response.get("response")).get("body");
            Object itemsObj = body.get("items"); if (itemsObj == null || itemsObj instanceof String) return roomList;
            Object itemData = ((Map<String, Object>) itemsObj).get("item");
            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) itemList = (List<Map<String, Object>>) itemData;
            else if (itemData instanceof Map) itemList.add((Map<String, Object>) itemData);
            for (Map<String, Object> item : itemList) {
                String title = getAnyField(item, "roomtitle", "roomTitle", "title"); if (title == null) continue;
                roomList.add(BookingResponse.RoomDTO.builder().name(title).content(valueToString(getAnyField(item, "roomintro", "roomIntro"))).baseCount(valueToString(getAnyField(item, "roombasecount", "roomBaseCount"))).maxCount(valueToString(getAnyField(item, "roommaxcount", "roomMaxCount"))).imageUrl(valueToString(getAnyField(item, "roomimg1", "roomImg1", "firstimage"))).build());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return roomList;
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchTourApiAdditionalImages(String serviceKey, String contentId) {
        List<String> images = new ArrayList<>();
        try {
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/detailImage2?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=TravelApp&_type=json&contentId=" + contentId + "&imageYN=Y&subImageYN=Y";
            String json = executeGet(urlStr); if (json == null) return images;
            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) response.get("response")).get("body");
            Object itemsObj = body.get("items"); if (itemsObj == null || itemsObj instanceof String) return images;
            Object itemData = ((Map<String, Object>) itemsObj).get("item");
            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) itemList = (List<Map<String, Object>>) itemData;
            else if (itemData instanceof Map) itemList.add((Map<String, Object>) itemData);
            for (Map<String, Object> item : itemList) {
                String img = getAnyField(item, "originimgurl", "originImgUrl", "smallimageurl"); if (img != null) images.add(img);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return images;
    }

    private String getAnyField(Map<String, Object> item, String... keys) {
        for (String key : keys) {
            Object val = item.get(key);
            if (val != null && !String.valueOf(val).isBlank()) return String.valueOf(val);
        }
        return null;
    }

    private String simplifyName(String name) { if (name == null) return ""; return name.replaceAll("\\s+", "").replaceAll("(호텔|리조트|펜션|모텔|스테이)", "").toLowerCase(); }
    private String valueToString(Object value) { return value == null ? "" : String.valueOf(value); }

    @Transactional
    public void processBookingCompletion(Integer userId, String lName, String rKey, String cIn, String cOut, String g, Integer price, String img) {
        User user = userRepository.findById(userId).orElse(null); if (user == null) return;
        Booking b = new Booking(); b.setUser(user); b.setLodgingName(lName); b.setCheckIn(LocalDate.parse(cIn)); b.setCheckOut(LocalDate.parse(cOut)); b.setTotalPrice(price == null ? 0 : price); b.setImageUrl(img); bookingRepository.save(b);
    }

    @Transactional
    public BookingResponse.PlaceImageDTO getPlaceImage(String serviceKey, String placeUrl, String name, String address) {
        String normalizedName = (name == null) ? "" : name.replaceAll("\\s+", "").toLowerCase();
        String imageUrl = bookingRepository.findImageUrlByNormalizedName(normalizedName).orElse(null);
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = fetchImageFromTourApi(serviceKey, name, address);
            if (imageUrl == null || imageUrl.isBlank()) {
                try {
                    Document doc = Jsoup.connect(placeUrl).userAgent("Mozilla/5.0").timeout(4000).get();
                    imageUrl = doc.select("meta[property=og:image]").attr("content");
                } catch (Exception e) { imageUrl = null; }
            }
            if (imageUrl != null && !imageUrl.isBlank()) bookingRepository.upsertMapPlaceImage(normalizedName, name, imageUrl, "KAKAO");
        }
        return BookingResponse.PlaceImageDTO.builder().imageUrl(imageUrl == null ? "" : imageUrl).name(name).build();
    }

    @SuppressWarnings("unchecked")
    private String fetchImageFromTourApi(String serviceKey, String name, String address) {
        try {
            String encodedKeyword = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/searchKeyword2?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=TravelApp&_type=json&keyword=" + encodedKeyword;
            String json = executeGet(urlStr); if (json == null) return null;
            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) response.get("response")).get("body");
            Object itemsObj = body.get("items"); if (itemsObj == null || itemsObj instanceof String) return null;
            Object itemData = ((Map<String, Object>) itemsObj).get("item");
            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) itemList = (List<Map<String, Object>>) itemData;
            else if (itemData instanceof Map) itemList.add((Map<String, Object>) itemData);
            if (!itemList.isEmpty()) return getAnyField(itemList.get(0), "firstimage", "firstimage2", "originimgurl");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<BookingResponse.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        // 1. 카카오 관광지 데이터 가져오기
        List<BookingResponse.MapPoiDTO> kakaoPois = new ArrayList<>();
        if (reqDTO.getKakaoPois() != null) {
            for (BookingRequest.MapPoiDTO req : reqDTO.getKakaoPois()) {
                kakaoPois.add(new BookingResponse.MapPoiDTO(req.getExternalPlaceId(), req.getName(), req.getPhone(), req.getAddress(), req.getRoadAddress(), req.getPlaceUrl(), req.getCategoryName(), req.getCategoryGroupCode(), req.getLat(), req.getLng(), req.getType(), "KAKAO"));
            }
        }

        // 2. DB 호텔 데이터 가져오기 (범위 제한 적용)
        double minLat = -90, maxLat = 90, minLng = -180, maxLng = 180;
        if (reqDTO.getBounds() != null) {
            minLat = Math.min(reqDTO.getBounds().getSwLat(), reqDTO.getBounds().getNeLat());
            maxLat = Math.max(reqDTO.getBounds().getSwLat(), reqDTO.getBounds().getNeLat());
            minLng = Math.min(reqDTO.getBounds().getSwLng(), reqDTO.getBounds().getNeLng());
            maxLng = Math.max(reqDTO.getBounds().getSwLng(), reqDTO.getBounds().getNeLng());
        }
        List<BookingResponse.MapPoiDTO> dbPois = bookingRepository.findActiveLodgingsInBounds(reqDTO.getRegionKey() == null ? "" : reqDTO.getRegionKey(), minLat, maxLat, minLng, maxLng);

        // 3. 중복 제거 및 통합 (LinkedHashMap 사용)
        LinkedHashMap<String, BookingResponse.MapPoiDTO> merged = new LinkedHashMap<>();
        // 관광지 먼저 넣기
        for (BookingResponse.MapPoiDTO p : kakaoPois) {
            String key = (p.getExternalPlaceId() != null && !p.getExternalPlaceId().isBlank()) ? p.getExternalPlaceId() : p.getName() + p.getLat() + p.getLng();
            merged.put(key, p);
        }
        // 호텔로 덮어쓰거나 추가하기 (호텔 우선순위)
        for (BookingResponse.MapPoiDTO p : dbPois) {
            String key = (p.getExternalPlaceId() != null && !p.getExternalPlaceId().isBlank()) ? p.getExternalPlaceId() : p.getName() + p.getLat() + p.getLng();
            merged.put(key, p);
        }

        return new ArrayList<>(merged.values());
    }

    @Transactional public void createBooking(Integer uId, BookingRequest.CreateBookingDTO d) {}
    @Transactional public void cancelBooking(Integer uId, Integer bId) {}
    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer uId) { return List.of(); }
    public BookingResponse.BookingDetailDTO getBookingDetail(Integer uId, Integer bId) { return null; }
}
