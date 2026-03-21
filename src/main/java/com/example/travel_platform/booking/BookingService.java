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
import com.example.travel_platform.trip.TripPlanQueryRepository;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.trip.TripRegion;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;
import tools.jackson.databind.ObjectMapper;

@Transactional(readOnly = true)
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserQueryRepository userQueryRepository;
    private final TripRepository tripRepository;
    private final TripPlanQueryRepository tripPlanQueryRepository;
    private final LodgingQueryRepository lodgingQueryRepository;
    private final MapPlaceImageRepository mapPlaceImageRepository;
    private final ObjectMapper objectMapper;

    // 명시적 생성자 주입
    public BookingService(
            BookingRepository bookingRepository,
            UserQueryRepository userQueryRepository,
            TripRepository tripRepository,
            TripPlanQueryRepository tripPlanQueryRepository,
            LodgingQueryRepository lodgingQueryRepository,
            MapPlaceImageRepository mapPlaceImageRepository) {
        this.bookingRepository = bookingRepository;
        this.userQueryRepository = userQueryRepository;
        this.tripRepository = tripRepository;
        this.tripPlanQueryRepository = tripPlanQueryRepository;
        this.lodgingQueryRepository = lodgingQueryRepository;
        this.mapPlaceImageRepository = mapPlaceImageRepository;
        this.objectMapper = new ObjectMapper();
    }

    public User getUserById(Integer id) {
        return userQueryRepository.findUser(id).orElse(null);
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
     * TourAPI 4.0을 사용하여 숙소의 객실 정보 및 추가 이미지를 가져옴.
     */
    public List<BookingResponse.RoomDTO> getRoomList(String serviceKey, BookingRequest.RoomQueryDTO reqDTO) {
        if (reqDTO == null) {
            return List.of();
        }
        return fetchRoomsFromTourApi(serviceKey, reqDTO.getLodgingName(), reqDTO.getAddress());
    }

    private List<BookingResponse.RoomDTO> fetchRoomsFromTourApi(String serviceKey, String lodgingName, String address) {
        try {
            String contentId = searchTourApiContentId(serviceKey, lodgingName, address);
            if (contentId == null)
                return List.of();

            // 1. 객실 상세 정보 가져오기 (이름, 기본 사진 등)
            List<BookingResponse.RoomDTO> rooms = fetchTourApiRoomDetails(serviceKey, contentId);

            // 2. 추가 이미지 정보 가져오기 (갤러리용 고화질 사진들)
            List<String> extraImages = fetchTourApiAdditionalImages(serviceKey, contentId);

            // 3. 만약 객실 정보가 아예 없다면, 추가 이미지를 활용해 가상의 객실이라도 생성 (데이터 보완)
            if (rooms.isEmpty() && !extraImages.isEmpty()) {
                BookingResponse.RoomDTO virtualRoom = BookingResponse.RoomDTO.createRoom(
                        "기본 객실",
                        "상세 정보는 숙소에 문의해 주세요.",
                        "",
                        "",
                        extraImages.get(0),
                        extraImages);
                return List.of(virtualRoom);
            }

            // 4. 기존 객실 정보가 있다면 각각에 추가 이미지 리스트를 넣어줌
            for (BookingResponse.RoomDTO room : rooms) {
                room.setAllImages(extraImages);
            }

            return rooms;
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchTourApiAdditionalImages(String serviceKey, String contentId) {
        List<String> images = new ArrayList<>();
        try {
            String urlStr = "https://apis.data.go.kr/B551011/KorService2/detailImage2"
                    + "?serviceKey=" + serviceKey
                    + "&MobileOS=ETC&MobileApp=TravelApp&_type=json"
                    + "&contentId=" + contentId
                    + "&imageYN=Y&subImageYN=Y";

            String json = executeGet(urlStr);
            if (json == null)
                return images;

            Map<String, Object> response = objectMapper.readValue(json, Map.class);
            Map<String, Object> res = (Map<String, Object>) response.get("response");
            Map<String, Object> body = (Map<String, Object>) res.get("body");
            if (body == null || body.get("items") == null || body.get("items").equals(""))
                return images;

            Map<String, Object> itemsObj = (Map<String, Object>) body.get("items");
            Object itemData = itemsObj.get("item");

            List<Map<String, Object>> itemList = new ArrayList<>();
            if (itemData instanceof List) {
                itemList = (List<Map<String, Object>>) itemData;
            } else if (itemData instanceof Map) {
                itemList.add((Map<String, Object>) itemData);
            }

            for (Map<String, Object> item : itemList) {
                // 이미지 필드명 후보들: originimgurl, smallimageurl, imageUrl 등
                String imgUrl = getAnyField(item, "originimgurl", "originImgUrl", "smallimageurl", "smallImageUrl",
                        "imageUrl");
                if (imgUrl != null && !imgUrl.isBlank()) {
                    images.add(imgUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("[TourAPI] 추가 이미지 조회 실패: " + e.getMessage());
        }
        return images;
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
                    + "&keyword=" + encodedKeyword;

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
    private List<BookingResponse.RoomDTO> fetchTourApiRoomDetails(String serviceKey, String contentId) {
        List<BookingResponse.RoomDTO> roomList = new ArrayList<>();
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
                // 필드명이 바뀔 수 있으므로 여러 가능성을 열어두고 추출
                String roomTitle = getAnyField(item, "roomtitle", "roomTitle", "title");
                if (roomTitle == null || roomTitle.isBlank())
                    continue;

                String roomIntro = getAnyField(item, "roomintro", "roomIntro", "content");
                String roomBaseCount = getAnyField(item, "roombasecount", "roomBaseCount", "baseCount");
                String roomMaxCount = getAnyField(item, "roommaxcount", "roomMaxCount", "maxCount");
                String roomImg = getAnyField(item, "roomimg1", "roomImg1", "roomimg", "imageUrl");

                roomList.add(BookingResponse.RoomDTO.createRoom(
                        roomTitle,
                        valueToString(roomIntro),
                        valueToString(roomBaseCount),
                        valueToString(roomMaxCount),
                        valueToString(roomImg),
                        List.of()));
            }
        } catch (Exception e) {
            System.err.println("[TourAPI] 객실 상세 조회 실패: " + e.getMessage());
        }
        return roomList;
    }

    private String getAnyField(Map<String, Object> item, String... keys) {
        for (String key : keys) {
            Object val = item.get(key);
            if (val != null && !String.valueOf(val).isBlank()) {
                return String.valueOf(val);
            }
        }
        return null;
    }

    @Transactional
    public void processBookingCompletion(
            Integer sessionUserId,
            BookingRequest.CompleteBookingDTO reqDTO) {
        User user = findUserOrNull(sessionUserId);
        if (user == null || reqDTO == null) {
            return;
        }

        BookingCompletionDraft draft = createCompletionDraft(reqDTO);
        TripPlan plan = resolveCompletionPlan(user, draft);
        Booking booking = buildCompletionBooking(user, plan, draft);
        bookingRepository.save(booking);
    }

    @Transactional
    public void createBooking(Integer sessionUserId, BookingRequest.CreateBookingDTO reqDTO) {
        User user = findUserOrNull(sessionUserId);
        if (user == null || reqDTO == null) {
            return;
        }

        TripPlan plan = findTripPlanOrNull(reqDTO.getTripPlanId());
        if (plan == null) {
            return;
        }

        bookingRepository.save(buildCreatedBooking(user, plan, reqDTO));
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
        keepPlaceholderCancel(sessionUserId, bookingId);
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        return buildPlaceholderBookingList(sessionUserId);
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        return buildPlaceholderBookingDetail(sessionUserId, bookingId);
    }

    @Transactional
    public BookingResponse.PlaceImageDTO getPlaceImage(String serviceKey, BookingRequest.PlaceImageQueryDTO reqDTO) {
        if (reqDTO == null) {
            return BookingResponse.PlaceImageDTO.createPlaceImage("", "");
        }

        String normalizedName = normalizePlaceName(reqDTO.getName());
        String imageUrl = findCachedPlaceImage(normalizedName);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = resolvePlaceImage(serviceKey, reqDTO);
            cachePlaceImage(normalizedName, reqDTO.getName(), imageUrl);
        }

        return BookingResponse.PlaceImageDTO.createPlaceImage(imageUrl, reqDTO.getName());
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

    public List<BookingResponse.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingResponse.MapPoiDTO> kakaoPois = reqDTO == null || reqDTO.getKakaoPois() == null ? List.of()
                : convertToResponsePois(reqDTO.getKakaoPois());
        String regionKey = reqDTO == null ? "" : blankToDefault(reqDTO.getRegionKey(), "");
        double[] bounds = resolveBounds(reqDTO == null ? null : reqDTO.getBounds());

        List<BookingResponse.MapPoiDTO> dbPois = lodgingQueryRepository.findActiveLodgingsInBounds(
                regionKey, bounds[0], bounds[1], bounds[2], bounds[3]);

        LinkedHashMap<String, BookingResponse.MapPoiDTO> merged = new LinkedHashMap<>();
        for (BookingResponse.MapPoiDTO item : kakaoPois) {
            BookingResponse.MapPoiDTO normalized = normalizePoi(item, "KAKAO");
            if (normalized != null)
                merged.put(buildPoiKey(normalized), normalized);
        }
        for (BookingResponse.MapPoiDTO item : dbPois) {
            BookingResponse.MapPoiDTO normalized = normalizePoi(item, "DB");
            if (normalized != null) {
                String key = buildPoiKey(normalized);
                BookingResponse.MapPoiDTO existing = merged.get(key);
                if (existing == null || "KAKAO".equals(existing.getSource()))
                    merged.put(key, normalized);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<BookingResponse.MapPoiDTO> convertToResponsePois(List<BookingRequest.MapPoiDTO> requestPois) {
        List<BookingResponse.MapPoiDTO> responsePois = new ArrayList<>();
        for (BookingRequest.MapPoiDTO req : requestPois) {
            responsePois.add(new BookingResponse.MapPoiDTO(
                    req.getExternalPlaceId(),
                    req.getName(),
                    req.getPhone(),
                    req.getAddress(),
                    req.getRoadAddress(),
                    req.getPlaceUrl(),
                    req.getCategoryName(),
                    req.getCategoryGroupCode(),
                    req.getLat(),
                    req.getLng(),
                    req.getType(),
                    req.getSource()));
        }
        return responsePois;
    }

    private LocalDate resolveDate(String dateText, LocalDate defaultDate) {
        try {
            return (dateText == null || dateText.isBlank()) ? defaultDate : LocalDate.parse(dateText);
        } catch (Exception e) {
            return defaultDate;
        }
    }

    private User findUserOrNull(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userQueryRepository.findUser(userId).orElse(null);
    }

    private TripPlan findTripPlanOrNull(Integer tripPlanId) {
        if (tripPlanId == null) {
            return null;
        }
        return tripPlanQueryRepository.findPlan(tripPlanId).orElse(null);
    }

    private BookingCompletionDraft createCompletionDraft(BookingRequest.CompleteBookingDTO reqDTO) {
        LocalDate checkInDate = resolveDate(reqDTO.getCheckIn(), LocalDate.now());
        LocalDate checkOutDate = resolveDate(reqDTO.getCheckOut(), LocalDate.now().plusDays(1));
        TripRegion regionType = resolveBookingRegion(reqDTO.getRegionKey(), reqDTO.getLocation());

        return new BookingCompletionDraft(
                blankToDefault(reqDTO.getLodgingName(), "숙소"),
                blankToDefault(reqDTO.getRoomName(), BookVar.DEFAULT_ROOM_NAME),
                regionType,
                checkInDate,
                checkOutDate,
                parseGuestCount(reqDTO.getGuests()),
                reqDTO.getPricePerNight() == null ? 0 : reqDTO.getPricePerNight(),
                reqDTO.getTaxAndServiceFee() == null ? 0 : reqDTO.getTaxAndServiceFee(),
                blankToDefault(reqDTO.getImageUrl(), ""));
    }

    private TripPlan resolveCompletionPlan(User user, BookingCompletionDraft draft) {
        List<TripPlan> plans = tripPlanQueryRepository.findPlanList(user.getId(), 0, 1);
        if (!plans.isEmpty()) {
            return plans.get(0);
        }
        return tripRepository.savePlan(createCompletionPlan(user, draft));
    }

    private TripPlan createCompletionPlan(User user, BookingCompletionDraft draft) {
        return TripPlan.create(
                user,
                "나의 여행 계획",
                draft.regionType(),
                null,
                draft.checkInDate(),
                draft.checkOutDate(),
                draft.imageUrl());
    }

    private Booking buildCompletionBooking(User user, TripPlan plan, BookingCompletionDraft draft) {
        return Booking.create(
                user,
                plan,
                draft.lodgingName(),
                draft.roomName(),
                draft.checkInDate(),
                draft.checkOutDate(),
                draft.guestCount(),
                draft.pricePerNight(),
                draft.taxAndServiceFee(),
                draft.regionType(),
                draft.imageUrl());
    }

    private Booking buildCreatedBooking(User user, TripPlan plan, BookingRequest.CreateBookingDTO reqDTO) {
        TripRegion regionType = resolveBookingRegion(reqDTO.getRegionKey(), reqDTO.getLocation());
        return Booking.create(
                user,
                plan,
                reqDTO.getLodgingName(),
                reqDTO.getRoomName(),
                reqDTO.getCheckIn(),
                reqDTO.getCheckOut(),
                reqDTO.getGuestCount(),
                reqDTO.getPricePerNight(),
                reqDTO.getTaxAndServiceFee(),
                regionType,
                reqDTO.getImageUrl());
    }

    private void keepPlaceholderCancel(Integer sessionUserId, Integer bookingId) {
        if (sessionUserId == null || bookingId == null) {
            return;
        }
    }

    private List<BookingResponse.BookingSummaryDTO> buildPlaceholderBookingList(Integer sessionUserId) {
        if (sessionUserId == null) {
            return List.of();
        }
        return List.of();
    }

    private BookingResponse.BookingDetailDTO buildPlaceholderBookingDetail(Integer sessionUserId, Integer bookingId) {
        if (sessionUserId == null || bookingId == null) {
            return null;
        }
        return null;
    }

    private String normalizePlaceName(String name) {
        return (name == null) ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private String findCachedPlaceImage(String normalizedName) {
        if (normalizedName.isBlank()) {
            return null;
        }
        return mapPlaceImageRepository.findImageUrlByNormalizedName(normalizedName).orElse(null);
    }

    private String resolvePlaceImage(String serviceKey, BookingRequest.PlaceImageQueryDTO reqDTO) {
        String imageUrl = fetchImageFromTourApi(serviceKey, reqDTO.getName(), reqDTO.getAddress());
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = resolveImageFromKakaoPlace(reqDTO.getPlaceUrl());
        }
        return imageUrl;
    }

    private void cachePlaceImage(String normalizedName, String name, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || normalizedName.isBlank()) {
            return;
        }
        mapPlaceImageRepository.upsertMapPlaceImage(
                normalizedName,
                blankToDefault(name, normalizedName),
                imageUrl,
                "TOUR_API_OR_KAKAO");
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

    private TripRegion resolveBookingRegion(String regionKey, String location) {
        TripRegion region = TripRegion.fromCodeOrNull(regionKey);
        if (region != null) {
            return region;
        }

        String locationText = location == null ? "" : location.trim();
        if (locationText.contains("서울")) {
            return TripRegion.SEOUL;
        }
        if (locationText.contains("부산")) {
            return TripRegion.BUSAN;
        }
        if (locationText.contains("대구")) {
            return TripRegion.DAEGU;
        }
        if (locationText.contains("인천")) {
            return TripRegion.INCHEON;
        }
        if (locationText.contains("광주")) {
            return TripRegion.GWANGJU;
        }
        if (locationText.contains("대전")) {
            return TripRegion.DAEJEON;
        }
        if (locationText.contains("울산")) {
            return TripRegion.ULSAN;
        }
        if (locationText.contains("세종")) {
            return TripRegion.SEJONG;
        }
        if (locationText.contains("경기")) {
            return TripRegion.GYEONGGI;
        }
        if (locationText.contains("강원")) {
            return TripRegion.GANGWON;
        }
        if (locationText.contains("충북")) {
            return TripRegion.CHUNGBUK;
        }
        if (locationText.contains("충남")) {
            return TripRegion.CHUNGNAM;
        }
        if (locationText.contains("전북")) {
            return TripRegion.JEONBUK;
        }
        if (locationText.contains("전남")) {
            return TripRegion.JEONNAM;
        }
        if (locationText.contains("경북")) {
            return TripRegion.GYEONGBUK;
        }
        if (locationText.contains("경남")) {
            return TripRegion.GYEONGNAM;
        }
        if (locationText.contains("제주")) {
            return TripRegion.JEJU;
        }
        return TripRegion.fromCode(BookVar.DEFAULT_REGION_KEY);
    }

    private BookingResponse.MapPoiDTO normalizePoi(BookingResponse.MapPoiDTO item, String defaultSource) {
        if (item == null || !isValidCoordinate(item.getLat()) || !isValidCoordinate(item.getLng()))
            return null;
        BookingResponse.MapPoiDTO poi = new BookingResponse.MapPoiDTO();
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

    private String buildPoiKey(BookingResponse.MapPoiDTO item) {
        if (item.getExternalPlaceId() != null && !item.getExternalPlaceId().isBlank())
            return "id:" + item.getExternalPlaceId();
        String normalized = (item.getName() == null) ? "" : item.getName().replaceAll("\\s+", "").toLowerCase();
        String lat = String.format("%.4f", item.getLat());
        String lng = String.format("%.4f", item.getLng());
        return "geo:" + normalized + ":" + lat + ":" + lng;
    }

    private String valueToString(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private record BookingCompletionDraft(
            String lodgingName,
            String roomName,
            TripRegion regionType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int guestCount,
            int pricePerNight,
            int taxAndServiceFee,
            String imageUrl) {
    }
}
