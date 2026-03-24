package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.booking.lodging.LodgingPoiRow;
import com.example.travel_platform.booking.lodging.LodgingQueryRepository;
import com.example.travel_platform.booking.mapPlaceImage.MapPlaceImageRepository;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripPlanQueryRepository;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.trip.TripRegion;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

@Transactional(readOnly = true)
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingQueryRepository bookingQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final TripRepository tripRepository;
    private final TripPlanQueryRepository tripPlanQueryRepository;
    private final LodgingQueryRepository lodgingQueryRepository;
    private final MapPlaceImageRepository mapPlaceImageRepository;

    // 명시적 생성자 주입
    public BookingService(
            BookingRepository bookingRepository,
            BookingQueryRepository bookingQueryRepository,
            UserQueryRepository userQueryRepository,
            TripRepository tripRepository,
            TripPlanQueryRepository tripPlanQueryRepository,
            LodgingQueryRepository lodgingQueryRepository,
            MapPlaceImageRepository mapPlaceImageRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingQueryRepository = bookingQueryRepository;
        this.userQueryRepository = userQueryRepository;
        this.tripRepository = tripRepository;
        this.tripPlanQueryRepository = tripPlanQueryRepository;
        this.lodgingQueryRepository = lodgingQueryRepository;
        this.mapPlaceImageRepository = mapPlaceImageRepository;
    }

    public User getUserById(Integer id) {
        return userQueryRepository.findUser(id).orElse(null);
    }

    public List<BookingResponse.RoomDTO> getRoomList(BookingRequest.RoomQueryDTO reqDTO) {
        if (reqDTO == null || reqDTO.getLodgingName() == null || reqDTO.getLodgingName().isBlank()) {
            return List.of();
        }

        String lodgingName = reqDTO.getLodgingName();
        String address = reqDTO.getAddress() == null ? "" : reqDTO.getAddress();
        int seed = Math.abs((lodgingName + "|" + address).hashCode());

        List<BookingResponse.RoomDTO> rooms = new ArrayList<>();
        String[] baseNames = new String[] { "스탠다드", "디럭스", "프리미엄" };
        String[] views = new String[] { "시티뷰", "오션뷰", "마운틴뷰" };
        String[] images = new String[] {
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800&h=520&fit=crop",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=520&fit=crop",
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&h=520&fit=crop"
        };

        for (int i = 0; i < baseNames.length; i += 1) {
            String roomName = baseNames[i] + " " + views[(seed + i) % views.length];
            rooms.add(BookingResponse.RoomDTO.createRoom(
                    roomName,
                    lodgingName + "에서 제공하는 " + baseNames[i] + " 객실입니다.",
                    String.valueOf(2),
                    String.valueOf(4),
                    images[i % images.length],
                    List.of(images)));
        }

        return rooms;
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
        if (plan == null && reqDTO.getCheckIn() != null && reqDTO.getCheckOut() != null) {
            plan = tripPlanQueryRepository.findByDates(user.getId(), reqDTO.getCheckIn(), reqDTO.getCheckOut())
                    .orElseGet(() -> {
                        TripRegion region = resolveBookingRegion(reqDTO.getRegionKey(), reqDTO.getLocation());
                        return tripRepository.savePlan(TripPlan.create(
                                user,
                                "나의 여행 계획",
                                region,
                                null,
                                reqDTO.getCheckIn(),
                                reqDTO.getCheckOut(),
                                reqDTO.getImageUrl()));
                    });
        }

        if (plan == null) {
            return;
        }

        bookingRepository.save(buildCreatedBooking(user, plan, reqDTO));
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
        Booking booking = findOwnedBooking(sessionUserId, bookingId);
        validateCancelableBooking(booking);
        booking.cancel(LocalDateTime.now());
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        List<Booking> bookings = bookingQueryRepository.findOwnedBookingList(sessionUserId);
        List<BookingResponse.BookingSummaryDTO> bookingSummaries = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingSummaries.add(BookingResponse.BookingSummaryDTO.fromBooking(booking));
        }
        return bookingSummaries;
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        return BookingResponse.BookingDetailDTO.fromBooking(findOwnedBooking(sessionUserId, bookingId));
    }

    @Transactional
    public BookingResponse.PlaceImageDTO getPlaceImage(BookingRequest.PlaceImageQueryDTO reqDTO) {
        if (reqDTO == null) {
            return BookingResponse.PlaceImageDTO.createPlaceImage("", "");
        }

        String normalizedName = normalizePlaceName(reqDTO.getName());
        String imageUrl = findCachedPlaceImage(normalizedName);

        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = resolvePlaceImage(reqDTO);
            cachePlaceImage(normalizedName, reqDTO.getName(), imageUrl);
        }

        return BookingResponse.PlaceImageDTO.createPlaceImage(imageUrl, reqDTO.getName());
    }

    public List<BookingResponse.MapPoiDTO> mergeMapPois(BookingRequest.MergeMapPoisDTO reqDTO) {
        List<BookingResponse.MapPoiDTO> kakaoPois = reqDTO == null || reqDTO.getKakaoPois() == null ? List.of()
                : convertToResponsePois(reqDTO.getKakaoPois());
        String regionKey = reqDTO == null ? "" : blankToDefault(reqDTO.getRegionKey(), "");
        double[] bounds = resolveBounds(reqDTO == null ? null : reqDTO.getBounds());

        List<LodgingPoiRow> dbPoiRows = lodgingQueryRepository.findActiveLodgingsInBounds(
                regionKey, bounds[0], bounds[1], bounds[2], bounds[3]);
        List<BookingResponse.MapPoiDTO> dbPois = createDbMapPois(dbPoiRows);

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

    private List<BookingResponse.MapPoiDTO> createDbMapPois(List<LodgingPoiRow> dbPoiRows) {
        List<BookingResponse.MapPoiDTO> dbPois = new ArrayList<>();
        for (LodgingPoiRow dbPoiRow : dbPoiRows) {
            dbPois.add(createDbMapPoi(dbPoiRow));
        }
        return dbPois;
    }

    private BookingResponse.MapPoiDTO createDbMapPoi(LodgingPoiRow dbPoiRow) {
        return BookingResponse.MapPoiDTO.createMapPoi(
                dbPoiRow.externalPlaceId(),
                dbPoiRow.name(),
                dbPoiRow.phone(),
                dbPoiRow.address(),
                dbPoiRow.roadAddress(),
                dbPoiRow.placeUrl(),
                dbPoiRow.categoryName(),
                dbPoiRow.categoryGroupCode(),
                dbPoiRow.lat(),
                dbPoiRow.lng(),
                "hotel",
                "DB");
    }

    private List<BookingResponse.MapPoiDTO> convertToResponsePois(List<BookingRequest.MapPoiDTO> requestPois) {
        List<BookingResponse.MapPoiDTO> responsePois = new ArrayList<>();
        for (BookingRequest.MapPoiDTO req : requestPois) {
            responsePois.add(BookingResponse.MapPoiDTO.createMapPoi(
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
        return tripPlanQueryRepository.findByDates(user.getId(), draft.checkInDate(), draft.checkOutDate())
                .orElseGet(() -> tripRepository.savePlan(createCompletionPlan(user, draft)));
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

    private Booking findOwnedBooking(Integer sessionUserId, Integer bookingId) {
        return bookingQueryRepository.findOwnedBooking(sessionUserId, bookingId)
                .orElseThrow(() -> new Exception404("예약 정보를 찾을 수 없습니다."));
    }

    private void validateCancelableBooking(Booking booking) {
        if (booking.isCancelled()) {
            throw new Exception400("이미 취소된 예약입니다.");
        }
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

    private String resolvePlaceImage(BookingRequest.PlaceImageQueryDTO reqDTO) {
        return resolveImageFromKakaoPlace(reqDTO.getPlaceUrl());
    }

    private void cachePlaceImage(String normalizedName, String name, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || normalizedName.isBlank()) {
            return;
        }
        mapPlaceImageRepository.upsertMapPlaceImage(
                normalizedName,
                blankToDefault(name, normalizedName),
                imageUrl,
                "KAKAO");
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
        return BookingResponse.MapPoiDTO.createNormalizedMapPoi(item, defaultSource);
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
