package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import com.example.travel_platform.user.UserQueryRepository;
import com.example.travel_platform.weather.WeatherRepository;

class TripServiceTest {

    @Test
    void create() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        User user = user(3, "ssar");
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("trip to jeju");
        reqDTO.setRegion("jeju");
        reqDTO.setWhoWith("friend");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 2));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 4));

        when(userQueryRepository.findUser(3)).thenReturn(Optional.of(user));
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
        assertNull(captor.getValue().getImgUrl());
        assertEquals(3, captor.getValue().getUser().getId());
    }

    @Test
    void createBadDate() {
        TripService tripService = service(
                mock(TripRepository.class),
                mock(TripPlanQueryRepository.class),
                mock(UserQueryRepository.class),
                mock(TripPlaceRepository.class),
                mock(WeatherRepository.class));
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("trip to busan");
        reqDTO.setRegion("busan");
        reqDTO.setWhoWith("family");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 5));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 3));

        Exception400 exception = assertThrows(Exception400.class, () -> tripService.createPlan(1, reqDTO));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    @Test
    void listResult() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        LocalDate today = LocalDate.now();
        TripPlan tripPlan = plan(7, user(2, "cos"), "tokyo trip", "seoul", "solo", today.plusDays(5),
                today.plusDays(7));

        when(tripPlanQueryRepository.findPlanList(2, 0, 9)).thenReturn(List.of(tripPlan));
        when(tripPlanQueryRepository.countPlanList(2)).thenReturn(11L);
        when(tripPlaceRepository.countByTripPlanIds(List.of(7))).thenReturn(java.util.Map.of(7, 3L));

        TripResponse.ListPageDTO response = tripService.getPlanList(2, "invalid", -1);

        assertEquals("result", response.getCategory());
        assertEquals(0, response.getCurrentPage());
        assertEquals(2, response.getTotalPage());
        assertTrue(response.isResult());
        assertFalse(response.isUpcoming());
        assertEquals(1, response.getPlans().size());
        assertEquals(3L, response.getPlans().get(0).getPlaceCount());
        assertEquals("/images/dumimg.jpg", response.getPlans().get(0).getImgUrl());
        verify(tripPlanQueryRepository).findPlanList(2, 0, 9);
        verify(tripPlanQueryRepository).countPlanList(2);
    }

    @Test
    void listUpcoming() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        LocalDate today = LocalDate.now();
        TripPlan tripPlan = plan(9, user(2, "cos"), "osaka trip", "busan", "friend", today.plusDays(10),
                today.plusDays(12));

        when(tripPlanQueryRepository.findUpcomingPlanList(eq(2), any(LocalDate.class), eq(9), eq(9)))
                .thenReturn(List.of(tripPlan));
        when(tripPlanQueryRepository.countUpcomingPlanList(eq(2), any(LocalDate.class))).thenReturn(1L);
        when(tripPlaceRepository.countByTripPlanIds(List.of(9))).thenReturn(java.util.Map.of(9, 2L));

        TripResponse.ListPageDTO response = tripService.getPlanList(2, "upcoming", 1);

        assertEquals("upcoming", response.getCategory());
        assertEquals(1, response.getCurrentPage());
        assertTrue(response.isUpcoming());
        assertFalse(response.isPast());
        assertEquals(2L, response.getPlans().get(0).getPlaceCount());
        verify(tripPlanQueryRepository).findUpcomingPlanList(eq(2), any(LocalDate.class), eq(9), eq(9));
        verify(tripPlanQueryRepository).countUpcomingPlanList(eq(2), any(LocalDate.class));
    }

    @Test
    void list401() {
        TripService tripService = service(
                mock(TripRepository.class),
                mock(TripPlanQueryRepository.class),
                mock(UserQueryRepository.class),
                mock(TripPlaceRepository.class),
                mock(WeatherRepository.class));

        Exception401 exception = assertThrows(Exception401.class, () -> tripService.getPlanList(null, "result", 0));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    @Test
    void detailSort() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        TripPlan tripPlan = plan(21, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        TripPlace first = place(40, tripPlan, "museum", 2);
        TripPlace second = place(10, tripPlan, "beach", 1);
        TripPlace third = place(30, tripPlan, "cafe", 1);
        TripPlace fourth = place(5, tripPlan, "hotel", 3);
        setField(tripPlan, "places", List.of(first, second, third, fourth));

        when(tripPlanQueryRepository.findPlanWithPlaces(21)).thenReturn(Optional.of(tripPlan));

        TripResponse.DetailDTO response = tripService.getPlanDetail(2, 21);

        assertTrue(response.isHasPlaces());
        assertEquals(4L, response.getPlaceCount());

        List<Integer> actualIds = response.getDays().stream()
                .flatMap(day -> day.getItems().stream())
                .map(TripResponse.PlaceItemDTO::getId)
                .toList();
        assertEquals(List.of(10, 30, 40, 5), actualIds);
    }

    @Test
    void detail403() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        TripPlan tripPlan = plan(21, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));

        when(tripPlanQueryRepository.findPlanWithPlaces(21)).thenReturn(Optional.of(tripPlan));

        Exception403 exception = assertThrows(Exception403.class, () -> tripService.getPlanDetail(8, 21));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    @Test
    void placePage() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        TripPlan tripPlan = plan(9, user(2, "cos"), "jeju plan", "jeju", "friend",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));
        setField(tripPlan, "places", List.of(place(11, tripPlan, "beach", 1), place(12, tripPlan, "hotel", 2)));

        when(tripPlanQueryRepository.findPlanWithPlaces(9)).thenReturn(Optional.of(tripPlan));

        TripResponse.PlacePageDTO response = tripService.getPlacePage(2, 9, "kakao-key");

        assertEquals(2L, response.getExistingCount());
        assertEquals("/trip/detail?id=9", response.getDetailUrl());
        assertEquals("/api/trips/9/places/bulk", response.getSaveUrl());
        assertEquals("kakao-key", response.getKakaoMapAppKey());
        assertEquals(2L, response.getDetail().getPlaceCount());
    }

    @Test
    void addPlace() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        TripPlan tripPlan = plan(17, user(4, "ssar"), "busan plan", "busan", "friend",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
        TripRequest.AddPlaceDTO reqDTO = new TripRequest.AddPlaceDTO();
        reqDTO.setPlaceName("harbor");
        reqDTO.setAddress("busan harbor");
        reqDTO.setLatitude(new BigDecimal("35.1000"));
        reqDTO.setLongitude(new BigDecimal("129.0400"));
        reqDTO.setDayOrder(3);

        when(tripPlanQueryRepository.findPlan(17)).thenReturn(Optional.of(tripPlan));
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
        assertEquals(3, captor.getValue().getTripDay());
    }

    @Test
    void add403() {
        TripRepository tripRepository = mock(TripRepository.class);
        TripPlanQueryRepository tripPlanQueryRepository = mock(TripPlanQueryRepository.class);
        UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
        TripPlaceRepository tripPlaceRepository = mock(TripPlaceRepository.class);
        WeatherRepository weatherRepository = mock(WeatherRepository.class);

        TripService tripService = service(tripRepository, tripPlanQueryRepository, userQueryRepository,
                tripPlaceRepository, weatherRepository);
        TripPlan tripPlan = plan(17, user(4, "ssar"), "busan plan", "busan", "friend",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
        TripRequest.AddPlaceDTO reqDTO = new TripRequest.AddPlaceDTO();
        reqDTO.setPlaceName("harbor");
        reqDTO.setAddress("busan harbor");
        reqDTO.setLatitude(new BigDecimal("35.1000"));
        reqDTO.setLongitude(new BigDecimal("129.0400"));
        reqDTO.setDayOrder(3);

        when(tripPlanQueryRepository.findPlan(17)).thenReturn(Optional.of(tripPlan));

        Exception403 exception = assertThrows(Exception403.class, () -> tripService.addPlace(8, 17, reqDTO));

        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
    }

    private TripService service(
            TripRepository tripRepository,
            TripPlanQueryRepository tripPlanQueryRepository,
            UserQueryRepository userQueryRepository,
            TripPlaceRepository tripPlaceRepository,
            WeatherRepository weatherRepository) {
        return new TripService(tripRepository, tripPlanQueryRepository, userQueryRepository, tripPlaceRepository,
                weatherRepository);
    }

    private User user(int id, String username) {
        User user = User.create(username, "1234", username + "@nate.com", "010-1111-2222", "USER");
        setField(user, "id", id);
        return user;
    }

    private TripPlan plan(
            int id,
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
