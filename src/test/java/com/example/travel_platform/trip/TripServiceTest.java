package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

class TripServiceTest {

    @Test
    void create() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        User user = user(3, "ssar");
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("trip to jeju");
        reqDTO.setRegion("jeju");
        reqDTO.setWhoWith("friend");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 2));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 4));

        when(userRepository.findById(3)).thenReturn(Optional.of(user));
        when(tripRepository.savePlan(any(TripPlan.class))).thenAnswer(invocation -> {
            TripPlan tripPlan = invocation.getArgument(0);
            setField(tripPlan, "id", 15);
            return tripPlan;
        });

        TripResponse.CreatedDTO response = tripService.createPlan(3, reqDTO);

        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripRepository).savePlan(captor.capture());
        assertEquals(15, response.getId());
        assertEquals("/trip/detail?id=15", response.getRedirectUrl());
        assertEquals("trip to jeju", captor.getValue().getTitle());
        assertEquals("jeju", captor.getValue().getRegion());
        assertEquals("friend", captor.getValue().getWhoWith());
        assertEquals("/images/dumimg.jpg", captor.getValue().getImgUrl());
        assertEquals(3, captor.getValue().getUser().getId());
    }

    @Test
    void createBadDate() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("trip to busan");
        reqDTO.setRegion("busan");
        reqDTO.setWhoWith("family");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 5));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 3));

        Exception400 exception = assertThrows(Exception400.class, () -> tripService.createPlan(1, reqDTO));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        verify(userRepository, never()).findById(any());
        verify(tripRepository, never()).savePlan(any());
    }

    @Test
    void listResult() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        LocalDate today = LocalDate.now();
        TripPlan tripPlan = plan(7, user(2, "cos"), "tokyo trip", "seoul", "solo", today.plusDays(5), today.plusDays(7));

        when(tripRepository.findPlanListByUserId(2, 0, 9)).thenReturn(List.of(tripPlan));
        when(tripRepository.countPlanByUserId(2)).thenReturn(11L);
        when(tripPlaceRepository.countByTripPlanId(7)).thenReturn(3L);

        TripResponse.ListPageDTO response = tripService.getPlanList(2, "invalid", -1);

        assertEquals("result", response.getCategory());
        assertEquals(0, response.getCurrentPage());
        assertEquals(2, response.getTotalPage());
        assertTrue(response.isResult());
        assertFalse(response.isUpcoming());
        assertEquals(1, response.getPlans().size());
        assertEquals(3L, response.getPlans().get(0).getPlaceCount());
        assertEquals("/images/dumimg.jpg", response.getPlans().get(0).getImgUrl());
        verify(tripRepository).findPlanListByUserId(2, 0, 9);
        verify(tripRepository).countPlanByUserId(2);
    }

    @Test
    void listUpcoming() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        LocalDate today = LocalDate.now();
        TripPlan tripPlan = plan(9, user(2, "cos"), "osaka trip", "busan", "friend", today.plusDays(10), today.plusDays(12));

        when(tripRepository.findUpcomingPlanListByUserId(eq(2), any(LocalDate.class), eq(9), eq(9)))
                .thenReturn(List.of(tripPlan));
        when(tripRepository.countUpcomingPlanByUserId(eq(2), any(LocalDate.class))).thenReturn(1L);
        when(tripPlaceRepository.countByTripPlanId(9)).thenReturn(2L);

        TripResponse.ListPageDTO response = tripService.getPlanList(2, "upcoming", 1);

        assertEquals("upcoming", response.getCategory());
        assertEquals(1, response.getCurrentPage());
        assertTrue(response.isUpcoming());
        assertFalse(response.isPast());
        assertEquals(2L, response.getPlans().get(0).getPlaceCount());
        verify(tripRepository).findUpcomingPlanListByUserId(eq(2), any(LocalDate.class), eq(9), eq(9));
        verify(tripRepository).countUpcomingPlanByUserId(eq(2), any(LocalDate.class));
    }

    @Test
    void list401() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);

        Exception401 exception = assertThrows(Exception401.class, () -> tripService.getPlanList(null, "result", 0));

        assertEquals("로그인이 필요합니다", exception.getMessage());
    }

    @Test
    void detailSort() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripPlan tripPlan = plan(21, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        TripPlace first = place(40, tripPlan, "museum", 2);
        TripPlace second = place(10, tripPlan, "beach", 1);
        TripPlace third = place(30, tripPlan, "cafe", 1);
        TripPlace fourth = place(5, tripPlan, "hotel", null);
        setField(tripPlan, "places", List.of(first, second, third, fourth));

        when(tripRepository.findPlanByIdWithPlaces(21)).thenReturn(Optional.of(tripPlan));

        TripResponse.DetailDTO response = tripService.getPlanDetail(2, 21);

        assertTrue(response.isHasPlaces());
        assertEquals(4L, response.getPlaceCount());
        assertEquals(List.of(10, 30, 40, 5),
                response.getPlaces().stream().map(item -> item.getId()).toList());
    }

    @Test
    void detail403() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripPlan tripPlan = plan(21, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));

        when(tripRepository.findPlanByIdWithPlaces(21)).thenReturn(Optional.of(tripPlan));

        Exception403 exception = assertThrows(Exception403.class, () -> tripService.getPlanDetail(9, 21));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    @Test
    void placePage() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripPlan tripPlan = plan(9, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        setField(tripPlan, "places", List.of(place(11, tripPlan, "beach", 1), place(12, tripPlan, "hotel", 2)));

        when(tripRepository.findPlanByIdWithPlaces(9)).thenReturn(Optional.of(tripPlan));

        TripResponse.PlacePageDTO response = tripService.getPlacePage(2, 9, "kakao-key");

        assertEquals(2L, response.getExistingCount());
        assertEquals("/trip/detail?id=9", response.getDetailUrl());
        assertEquals("/api/trips/9/places", response.getSaveUrl());
        assertEquals("kakao-key", response.getKakaoMapAppKey());
        assertEquals(2, response.getDetail().getPlaces().size());
    }

    @Test
    void addPlace() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripPlan tripPlan = plan(17, user(4, "ssar"), "busan plan", "busan", "friend",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
        TripRequest.AddPlaceDTO reqDTO = new TripRequest.AddPlaceDTO();
        reqDTO.setPlaceName("harbor");
        reqDTO.setAddress("busan harbor");
        reqDTO.setLatitude(new BigDecimal("35.1000"));
        reqDTO.setLongitude(new BigDecimal("129.0400"));
        reqDTO.setDayOrder(3);

        when(tripRepository.findPlanById(17)).thenReturn(Optional.of(tripPlan));
        when(tripPlaceRepository.save(any(TripPlace.class))).thenAnswer(invocation -> {
            TripPlace tripPlace = invocation.getArgument(0);
            setField(tripPlace, "id", 31);
            return tripPlace;
        });
        when(tripPlaceRepository.countByTripPlanId(17)).thenReturn(5L);

        TripResponse.PlaceAddedDTO response = tripService.addPlace(4, 17, reqDTO);

        ArgumentCaptor<TripPlace> captor = ArgumentCaptor.forClass(TripPlace.class);
        verify(tripPlaceRepository).save(captor.capture());
        assertEquals(31, response.getId());
        assertEquals(17, response.getPlanId());
        assertEquals("harbor", response.getPlaceName());
        assertEquals(3, response.getDayOrder());
        assertEquals(5L, response.getPlaceCount());
        assertEquals("/trip/detail?id=17", response.getDetailUrl());
        assertEquals("busan harbor", captor.getValue().getAddress());
        assertEquals(3, captor.getValue().getDayOrder());
    }

    @Test
    void add403() {
        TripRepository tripRepository = mock(TripRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        TripService tripService = new TripService(tripRepository, userRepository, tripPlaceRepository);
        TripPlan tripPlan = plan(17, user(4, "ssar"), "busan plan", "busan", "friend",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
        TripRequest.AddPlaceDTO reqDTO = new TripRequest.AddPlaceDTO();
        reqDTO.setPlaceName("harbor");
        reqDTO.setAddress("busan harbor");
        reqDTO.setLatitude(new BigDecimal("35.1000"));
        reqDTO.setLongitude(new BigDecimal("129.0400"));
        reqDTO.setDayOrder(3);

        when(tripRepository.findPlanById(17)).thenReturn(Optional.of(tripPlan));

        Exception403 exception = assertThrows(Exception403.class, () -> tripService.addPlace(8, 17, reqDTO));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    private User user(int id, String username) {
        User user = User.create(username, "1234", username + "@nate.com", "010-1111-2222", "USER");
        setField(user, "id", id);
        return user;
    }

    private TripPlan plan(int id,
            User user,
            String title,
            String region,
            String whoWith,
            LocalDate startDate,
            LocalDate endDate) {
        TripPlan tripPlan = TripPlan.create(user, title, region, whoWith, startDate, endDate, "");
        setField(tripPlan, "id", id);
        return tripPlan;
    }

    private TripPlace place(int id, TripPlan tripPlan, String placeName, Integer dayOrder) {
        TripPlace tripPlace = TripPlace.create(
                tripPlan,
                placeName,
                placeName + " address",
                new BigDecimal("33.1234"),
                new BigDecimal("126.1234"),
                dayOrder);
        setField(tripPlace, "id", id);
        return tripPlace;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
