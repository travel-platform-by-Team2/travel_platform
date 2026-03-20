package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class TripControllerTest {

    @Test
    void list() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(3);
        TripController controller = new TripController(tripService, session, "kakao-key");
        Model model = new ExtendedModelMap();
        TripResponse.ListPageDTO pageDTO = TripResponse.ListPageDTO.builder().build();

        when(tripService.getPlanList(3, "result", 2)).thenReturn(pageDTO);

        String view = controller.tripListPage("result", 2, model);

        assertEquals("pages/trip-list", view);
        assertSame(pageDTO, model.getAttribute("page"));
        verify(tripService).getPlanList(3, "result", 2);
    }

    @Test
    void createPage() {
        TripService tripService = mock(TripService.class);
        TripController controller = new TripController(tripService, new MockHttpSession(), "kakao-key");
        Model model = new ExtendedModelMap();

        String view = controller.tripCreatePage(model);

        assertEquals("pages/trip-create", view);
        assertEquals("", ((TripResponse.CreateFormDTO) model.getAttribute("page")).getTitle());
        verifyNoInteractions(tripService);
    }

    @Test
    void detail() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(4);
        TripController controller = new TripController(tripService, session, "kakao-key");
        Model model = new ExtendedModelMap();
        TripResponse.DetailDTO detailDTO = TripResponse.DetailDTO.builder().id(19).title("제주 여행").build();

        when(tripService.getPlanDetail(4, 19)).thenReturn(detailDTO);

        String view = controller.tripDetailPage(19, model);

        assertEquals("pages/trip-detail", view);
        assertSame(detailDTO, model.getAttribute("plan"));
        verify(tripService).getPlanDetail(4, 19);
    }

    @Test
    void place() {
        TripService tripService = mock(TripService.class);
        MockHttpSession session = session(8);
        TripController controller = new TripController(tripService, session, "kakao-key");
        Model model = new ExtendedModelMap();
        TripResponse.PlacePageDTO pageDTO = TripResponse.PlacePageDTO.builder().detailUrl("/trip/detail?id=7").build();

        when(tripService.getPlacePage(8, 7, "kakao-key")).thenReturn(pageDTO);

        String view = controller.tripAddPlacePage(7, model);

        assertEquals("pages/trip-add-place", view);
        assertSame(pageDTO, model.getAttribute("page"));
        verify(tripService).getPlacePage(8, 7, "kakao-key");
    }

    @Test
    void createErr() {
        TripService tripService = mock(TripService.class);
        TripController controller = new TripController(tripService, new MockHttpSession(), "kakao-key");
        Model model = new ExtendedModelMap();
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("제주 여행");
        reqDTO.setRegion("jeju");
        reqDTO.setWhoWith("친구");
        reqDTO.setStartDate(LocalDate.of(2026, 3, 22));
        BindingResult bindingResult = new BeanPropertyBindingResult(reqDTO, "reqDTO");
        bindingResult.rejectValue("endDate", "NotNull", "여행 종료일을 선택해주세요.");

        String view = controller.createPlan(reqDTO, bindingResult, model);

        TripResponse.CreateFormDTO page = (TripResponse.CreateFormDTO) model.getAttribute("page");
        assertEquals("pages/trip-create", view);
        assertEquals("제주 여행", page.getTitle());
        assertEquals("jeju", page.getRegion());
        assertEquals("친구", page.getWhoWith());
        assertEquals("여행 종료일을 선택해주세요.", page.getEndDateError());
        verifyNoInteractions(tripService);
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
