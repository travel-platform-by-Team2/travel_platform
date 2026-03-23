package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.booking.lodging.LodgingPoiRow;
import com.example.travel_platform.booking.lodging.LodgingQueryRepository;
import com.example.travel_platform.booking.mapPlaceImage.MapPlaceImageRepository;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripPlanQueryRepository;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

class BookingServiceTest {

    @Test
    void donePlan() {
        TestFixture fixture = fixture();
        User user = user(3);
        TripPlan plan = TripPlan.create(
                user,
                "부산 여행",
                "busan",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                "");

        when(fixture.userQueryRepository.findUser(3)).thenReturn(Optional.of(user));
        when(fixture.tripPlanQueryRepository.findPlanList(3, 0, 1)).thenReturn(List.of(plan));

        fixture.service.processBookingCompletion(3, BookingRequest.CompleteBookingDTO.builder()
                .lodgingName("시그니엘 부산")
                .roomName("디럭스 룸")
                .regionKey("busan")
                .location("부산")
                .checkIn("2026-04-10")
                .checkOut("2026-04-12")
                .guests("성인 2명")
                .pricePerNight(450000)
                .taxAndServiceFee(200000)
                .imageUrl("https://image.test/hotel.jpg")
                .build());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(fixture.bookingRepository).save(captor.capture());
        verify(fixture.tripRepository, never()).savePlan(any(TripPlan.class));

        Booking booking = captor.getValue();
        assertSame(user, booking.getUser());
        assertSame(plan, booking.getTripPlan());
        assertEquals("시그니엘 부산", booking.getLodgingName());
        assertEquals("디럭스 룸", booking.getRoomName());
        assertEquals(LocalDate.of(2026, 4, 10), booking.getCheckIn());
        assertEquals(LocalDate.of(2026, 4, 12), booking.getCheckOut());
        assertEquals(2, booking.getGuestCount());
        assertEquals(450000, booking.getPricePerNight());
        assertEquals(200000, booking.getTaxAndServiceFee());
        assertEquals("busan", booking.getRegionKey());
        assertEquals("부산", booking.getLocation());
        assertEquals(BookingStatus.BOOKED, booking.getStatus());
    }

    @Test
    void doneNew() {
        TestFixture fixture = fixture();
        User user = user(5);
        TripPlan savedPlan = TripPlan.create(
                user,
                "나의 여행 계획",
                "seoul",
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                "https://image.test/complete.jpg");

        when(fixture.userQueryRepository.findUser(5)).thenReturn(Optional.of(user));
        when(fixture.tripPlanQueryRepository.findPlanList(5, 0, 1)).thenReturn(List.of());
        when(fixture.tripRepository.savePlan(any(TripPlan.class))).thenReturn(savedPlan);

        fixture.service.processBookingCompletion(5, BookingRequest.CompleteBookingDTO.builder()
                .lodgingName("롯데 호텔")
                .roomName("프리미어 룸")
                .regionKey("seoul")
                .location("서울")
                .checkIn("2026-06-01")
                .checkOut("2026-06-03")
                .guests("성인 3명")
                .pricePerNight(500000)
                .taxAndServiceFee(250000)
                .imageUrl("https://image.test/complete.jpg")
                .build());

        ArgumentCaptor<TripPlan> planCaptor = ArgumentCaptor.forClass(TripPlan.class);
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(fixture.tripRepository).savePlan(planCaptor.capture());
        verify(fixture.bookingRepository).save(bookingCaptor.capture());

        TripPlan createdPlan = planCaptor.getValue();
        assertEquals("나의 여행 계획", createdPlan.getTitle());
        assertEquals("seoul", createdPlan.getRegion());
        assertEquals(LocalDate.of(2026, 6, 1), createdPlan.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 3), createdPlan.getEndDate());

        Booking booking = bookingCaptor.getValue();
        assertSame(savedPlan, booking.getTripPlan());
        assertEquals("프리미어 룸", booking.getRoomName());
        assertEquals("seoul", booking.getRegionKey());
        assertEquals("서울", booking.getLocation());
        assertEquals(3, booking.getGuestCount());
        assertEquals(BookingStatus.BOOKED, booking.getStatus());
    }

    @Test
    void create() {
        TestFixture fixture = fixture();
        User user = user(8);
        TripPlan plan = TripPlan.create(
                user,
                "제주 여행",
                "jeju",
                null,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                "");
        BookingRequest.CreateBookingDTO reqDTO = new BookingRequest.CreateBookingDTO();
        reqDTO.setTripPlanId(11);
        reqDTO.setLodgingName("제주 스테이");
        reqDTO.setRoomName("오션 스위트");
        reqDTO.setCheckIn(LocalDate.of(2026, 7, 10));
        reqDTO.setCheckOut(LocalDate.of(2026, 7, 12));
        reqDTO.setGuestCount(2);
        reqDTO.setPricePerNight(300000);
        reqDTO.setTaxAndServiceFee(90000);
        reqDTO.setRegionKey("jeju");
        reqDTO.setLocation("제주");
        reqDTO.setImageUrl("https://image.test/jeju.jpg");

        when(fixture.userQueryRepository.findUser(8)).thenReturn(Optional.of(user));
        when(fixture.tripPlanQueryRepository.findPlan(11)).thenReturn(Optional.of(plan));

        fixture.service.createBooking(8, reqDTO);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(fixture.bookingRepository).save(captor.capture());

        Booking booking = captor.getValue();
        assertSame(user, booking.getUser());
        assertSame(plan, booking.getTripPlan());
        assertEquals("제주 스테이", booking.getLodgingName());
        assertEquals("오션 스위트", booking.getRoomName());
        assertEquals(2, booking.getGuestCount());
        assertEquals("jeju", booking.getRegionKey());
        assertEquals("제주", booking.getLocation());
        assertEquals(BookingStatus.BOOKED, booking.getStatus());
    }

    @Test
    void list() {
        TestFixture fixture = fixture();
        Booking booking = booking(1, 11, 1);
        booking.setCreatedAt(LocalDateTime.of(2026, 3, 21, 9, 0));

        when(fixture.bookingQueryRepository.findOwnedBookingList(1)).thenReturn(List.of(booking));

        List<BookingResponse.BookingSummaryDTO> result = fixture.service.getBookingList(1);

        assertEquals(1, result.size());
        assertEquals(11, result.get(0).getId());
        assertEquals("booked", result.get(0).getStatusCode());
        assertEquals("예약 확정", result.get(0).getStatusLabel());
        assertEquals(330400, result.get(0).getTotalPrice());
        assertEquals("330,400원", result.get(0).getTotalPriceText());
    }

    @Test
    void detail() {
        TestFixture fixture = fixture();
        Booking booking = booking(1, 44, 9);
        booking.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));

        when(fixture.bookingQueryRepository.findOwnedBooking(1, 44)).thenReturn(Optional.of(booking));

        BookingResponse.BookingDetailDTO result = fixture.service.getBookingDetail(1, 44);

        assertEquals(44, result.getId());
        assertEquals(9, result.getTripPlanId());
        assertEquals("booked", result.getStatusCode());
        assertEquals("예약 확정", result.getStatusLabel());
        assertTrue(result.isCanCancel());
        assertEquals(330400, result.getTotalPrice());
    }

    @Test
    void detail404() {
        TestFixture fixture = fixture();

        when(fixture.bookingQueryRepository.findOwnedBooking(1, 99)).thenReturn(Optional.empty());

        assertThrows(Exception404.class, () -> fixture.service.getBookingDetail(1, 99));
    }

    @Test
    void cancel() {
        TestFixture fixture = fixture();
        Booking booking = booking(1, 77, 3);

        when(fixture.bookingQueryRepository.findOwnedBooking(1, 77)).thenReturn(Optional.of(booking));

        fixture.service.cancelBooking(1, 77);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(booking.getStatus().canCancel());
        verify(fixture.bookingQueryRepository).findOwnedBooking(1, 77);
    }

    @Test
    void cancel400() {
        TestFixture fixture = fixture();
        Booking booking = booking(1, 77, 3);
        booking.cancel(LocalDateTime.of(2026, 3, 21, 11, 0));

        when(fixture.bookingQueryRepository.findOwnedBooking(1, 77)).thenReturn(Optional.of(booking));

        assertThrows(Exception400.class, () -> fixture.service.cancelBooking(1, 77));
    }

    @Test
    void img() {
        BookingResponse.PlaceImageDTO dto = BookingResponse.PlaceImageDTO.createPlaceImage(null, null);

        assertEquals("", dto.getImageUrl());
        assertEquals("", dto.getName());
    }

    @Test
    void mergeDbPois() {
        TestFixture fixture = fixture();
        BookingRequest.MergeMapPoisDTO reqDTO = new BookingRequest.MergeMapPoisDTO();
        reqDTO.setRegionKey("jeju");
        reqDTO.setBounds(new BookingRequest.MapBoundsDTO(33.10, 126.10, 33.90, 126.90));
        reqDTO.setKakaoPois(List.of());

        when(fixture.lodgingQueryRepository.findActiveLodgingsInBounds("jeju", 33.10, 33.90, 126.10, 126.90))
                .thenReturn(List.of(new LodgingPoiRow(
                        "lodging-1",
                        "제주 오션뷰 호텔",
                        "064-111-2222",
                        "제주 제주시",
                        "제주 제주시 해안로",
                        "https://lodging.test/1",
                        "숙소",
                        "AD5",
                        33.50,
                        126.50)));

        List<BookingResponse.MapPoiDTO> result = fixture.service.mergeMapPois(reqDTO);

        assertEquals(1, result.size());
        assertEquals("lodging-1", result.get(0).getExternalPlaceId());
        assertEquals("제주 오션뷰 호텔", result.get(0).getName());
        assertEquals("hotel", result.get(0).getType());
        assertEquals("DB", result.get(0).getSource());
    }

    private TestFixture fixture() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        BookingQueryRepository bookingQueryRepository = mock(BookingQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        LodgingQueryRepository lodgingQueryRepository = mock(LodgingQueryRepository.class);
        MapPlaceImageRepository mapPlaceImageRepository = mock(MapPlaceImageRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                bookingQueryRepository,
                userQueryRepository,
                tripRepository,
                tripPlanQueryRepository,
                lodgingQueryRepository,
                mapPlaceImageRepository);
        return new TestFixture(
                bookingRepository,
                bookingQueryRepository,
                userQueryRepository,
                tripRepository,
                tripPlanQueryRepository,
                lodgingQueryRepository,
                service);
    }

    private User user(Integer id) {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(id);
        return user;
    }

    private Booking booking(Integer userId, Integer bookingId, Integer tripPlanId) {
        User user = user(userId);
        TripPlan tripPlan = TripPlan.create(
                user,
                "제주 여행",
                "jeju",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                null);
        tripPlan.setId(tripPlanId);

        Booking booking = Booking.create(
                user,
                tripPlan,
                "제주 오션뷰 호텔",
                "오션뷰 스탠다드",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                2,
                280000,
                50400,
                "jeju",
                "https://image.test/hotel.jpg");
        booking.setId(bookingId);
        return booking;
    }

    private record TestFixture(
            BookingRepository bookingRepository,
            BookingQueryRepository bookingQueryRepository,
            UserQueryRepository userQueryRepository,
            TripRepository tripRepository,
            TripPlanQueryRepository tripPlanQueryRepository,
            LodgingQueryRepository lodgingQueryRepository,
            BookingService service) {
    }
}
