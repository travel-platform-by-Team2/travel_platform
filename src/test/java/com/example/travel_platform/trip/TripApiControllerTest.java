package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class TripApiControllerTest {

    @Test
    void createOk() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(5);
        TripApiController controller = new TripApiController(tripService, session);
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("부산 여행");
        reqDTO.setRegion("busan");
        reqDTO.setWhoWith("친구");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 1));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 3));
        TripResponse.CreatedDTO responseDTO = TripResponse.CreatedDTO.createCreatedPlan(11);

        when(tripService.createPlan(5, reqDTO)).thenReturn(responseDTO);

        ResponseEntity<Resp<TripResponse.CreatedDTO>> response = controller.createPlan(reqDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertSame(responseDTO, response.getBody().getBody());
        verify(tripService).createPlan(5, reqDTO);
    }

    @Test
    void listOk() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(6);
        TripApiController controller = new TripApiController(tripService, session);
        TripResponse.ListPageDTO responseDTO = TripResponse.ListPageDTO.builder().build();

        when(tripService.getPlanList(6, "upcoming", 1)).thenReturn(responseDTO);

        ResponseEntity<Resp<TripResponse.ListPageDTO>> response = controller.getPlanList("upcoming", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody().getBody());
        verify(tripService).getPlanList(6, "upcoming", 1);
    }

    @Test
    void addOk() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(7);
        TripApiController controller = new TripApiController(tripService, session);
        TripRequest.AddPlaceDTO reqDTO = new TripRequest.AddPlaceDTO();
        reqDTO.setPlaceName("성산 일출봉");
        reqDTO.setAddress("제주도 제주시");
        reqDTO.setLatitude(new BigDecimal("33.3949"));
        reqDTO.setLongitude(new BigDecimal("126.2394"));
        reqDTO.setDayOrder(2);
        TripResponse.PlaceAddedDTO responseDTO = TripResponse.PlaceAddedDTO.builder()
                .id(21)
                .planId(9)
                .placeName("성산 일출봉")
                .dayOrder(2)
                .placeCount(4)
                .build();

        when(tripService.addPlace(7, 9, reqDTO)).thenReturn(responseDTO);

        ResponseEntity<Resp<TripResponse.PlaceAddedDTO>> response = controller.addPlace(9, reqDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody().getBody());
        verify(tripService).addPlace(7, 9, reqDTO);
    }

    @Test
    void api401() {
        TripService tripService = mock(TripService.class);
        TripApiController controller = new TripApiController(tripService, new MockHttpSession());

        Exception401 exception = assertThrows(Exception401.class, () -> controller.getPlanList("result", 0));

        assertTrue(exception.getMessage() != null && exception.getMessage().contains("로그인"));
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
