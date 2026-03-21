package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripPlanQueryRepository;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

class BookingServiceTest {

    @Test
    void donePlan() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        LodgingQueryRepository lodgingQueryRepository = mock(LodgingQueryRepository.class);
        MapPlaceImageRepository mapPlaceImageRepository = mock(MapPlaceImageRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                tripPlanQueryRepository,
                lodgingQueryRepository,
                mapPlaceImageRepository);

        User user = user(3);
        TripPlan plan = TripPlan.create(
                user,
                "부산 여행",
                "busan",
                null,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                "");

        when(userQueryRepository.findUser(3)).thenReturn(Optional.of(user));
        when(tripPlanQueryRepository.findPlanList(3, 0, 1)).thenReturn(List.of(plan));

        service.processBookingCompletion(3, BookingRequest.CompleteBookingDTO.builder()
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
        verify(bookingRepository).save(captor.capture());
        verify(tripRepository, never()).savePlan(any(TripPlan.class));

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
    }

    @Test
    void doneNew() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        LodgingQueryRepository lodgingQueryRepository = mock(LodgingQueryRepository.class);
        MapPlaceImageRepository mapPlaceImageRepository = mock(MapPlaceImageRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                tripPlanQueryRepository,
                lodgingQueryRepository,
                mapPlaceImageRepository);

        User user = user(5);
        TripPlan savedPlan = TripPlan.create(
                user,
                "나의 여행 계획",
                "seoul",
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                "https://image.test/complete.jpg");

        when(userQueryRepository.findUser(5)).thenReturn(Optional.of(user));
        when(tripPlanQueryRepository.findPlanList(5, 0, 1)).thenReturn(List.of());
        when(tripRepository.savePlan(any(TripPlan.class))).thenReturn(savedPlan);

        service.processBookingCompletion(5, BookingRequest.CompleteBookingDTO.builder()
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
        verify(tripRepository).savePlan(planCaptor.capture());
        verify(bookingRepository).save(bookingCaptor.capture());

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
    }

    @Test
    void create() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        LodgingQueryRepository lodgingQueryRepository = mock(LodgingQueryRepository.class);
        MapPlaceImageRepository mapPlaceImageRepository = mock(MapPlaceImageRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                tripPlanQueryRepository,
                lodgingQueryRepository,
                mapPlaceImageRepository);

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

        when(userQueryRepository.findUser(8)).thenReturn(Optional.of(user));
        when(tripPlanQueryRepository.findPlan(11)).thenReturn(Optional.of(plan));

        service.createBooking(8, reqDTO);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());

        Booking booking = captor.getValue();
        assertSame(user, booking.getUser());
        assertSame(plan, booking.getTripPlan());
        assertEquals("제주 스테이", booking.getLodgingName());
        assertEquals("오션 스위트", booking.getRoomName());
        assertEquals(2, booking.getGuestCount());
        assertEquals("jeju", booking.getRegionKey());
        assertEquals("제주", booking.getLocation());
    }

    @Test
    void img() {
        BookingResponse.PlaceImageDTO dto = BookingResponse.PlaceImageDTO.createPlaceImage(null, null);

        assertEquals("", dto.getImageUrl());
        assertEquals("", dto.getName());
    }

    @Test
    void cancelPh() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                mock(TripPlanQueryRepository.class),
                mock(LodgingQueryRepository.class),
                mock(MapPlaceImageRepository.class));

        service.cancelBooking(1, 99);

        verifyNoInteractions(bookingRepository, userQueryRepository, tripRepository);
    }

    @Test
    void listPh() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                mock(TripPlanQueryRepository.class),
                mock(LodgingQueryRepository.class),
                mock(MapPlaceImageRepository.class));

        assertEquals(List.of(), service.getBookingList(1));
    }

    @Test
    void detailPh() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripRepository tripRepository = mock(TripRepository.class);
        BookingService service = new BookingService(
                bookingRepository,
                userQueryRepository,
                tripRepository,
                mock(TripPlanQueryRepository.class),
                mock(LodgingQueryRepository.class),
                mock(MapPlaceImageRepository.class));

        assertNull(service.getBookingDetail(1, 44));
    }

    private User user(Integer id) {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");
        user.setId(id);
        return user;
    }
}
