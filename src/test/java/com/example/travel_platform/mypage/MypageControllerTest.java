package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.booking.BookingResponse;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.UserService;

class MypageControllerTest {

    @Test
    void main() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(3, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.createPasswordSuccessPage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()),
                "password changed");

        when(mypageService.getPasswordSuccessMainPage(3, "password changed")).thenReturn(pageDTO);

        String view = controller.showMainPage("password changed", model);

        assertEquals("pages/mypage", view);
        assertSame(pageDTO, model.getAttribute("model"));
        assertEquals("password changed", pageDTO.getPasswordSuccessMessage());
        assertNull(pageDTO.getPasswordError());
        assertNull(pageDTO.getWithdrawError());
        assertFalse(pageDTO.isPasswordModalOpen());
        assertFalse(pageDTO.isWithdrawModalOpen());
        verify(mypageService).getPasswordSuccessMainPage(3, "password changed");
        verifyNoInteractions(userService);
    }

    @Test
    void bookingList() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(3, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();

        MypageResponse.BookingListPageDTO page = MypageResponse.BookingListPageDTO.createBookingListPage(
                BookingCategory.UPCOMING,
                true);
        List<MypageResponse.BookingListCardDTO> items = List.of(
                MypageResponse.BookingListCardDTO.builder()
                        .id(21)
                        .detailLink("/mypage/bookings/21")
                        .build());

        when(mypageService.getBookingListView(3, "upcoming"))
                .thenReturn(MypageResponse.BookingListViewDTO.createBookingListView(page, items));

        String view = controller.showBookingListPage("upcoming", model);

        assertEquals("pages/booking-list", view);
        assertSame(page, model.getAttribute("model"));
        assertSame(items, model.getAttribute("models"));
        verify(mypageService).getBookingListView(3, "upcoming");
        verifyNoInteractions(userService);
    }

    @Test
    void pwdErr() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(5, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        MypageRequest.ChangePasswordDTO reqDTO = new MypageRequest.ChangePasswordDTO();
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.createPasswordFailurePage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()),
                "wrong password");

        doThrow(new Exception400("wrong password"))
                .when(mypageService)
                .changePassword(5, reqDTO);
        when(mypageService.getPasswordFailureMainPage(5, "wrong password")).thenReturn(pageDTO);

        String view = controller.changePassword(reqDTO, model, redirectAttributes);

        MypageResponse.MainPageDTO page = (MypageResponse.MainPageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("wrong password", page.getPasswordError());
        assertTrue(page.isPasswordModalOpen());
        assertFalse(page.isWithdrawModalOpen());
        verify(mypageService).changePassword(5, reqDTO);
        verify(mypageService).getPasswordFailureMainPage(5, "wrong password");
        verifyNoInteractions(userService);
    }

    @Test
    void pwdOk() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(7, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        MypageRequest.ChangePasswordDTO reqDTO = new MypageRequest.ChangePasswordDTO();

        String view = controller.changePassword(reqDTO, model, redirectAttributes);

        assertEquals("redirect:/mypage", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("passwordSuccessMessage"));
        verify(mypageService).changePassword(7, reqDTO);
        verifyNoInteractions(userService);
    }

    @Test
    void wdErr() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(9, "ADMIN");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();
        MypageRequest.WithdrawDTO reqDTO = new MypageRequest.WithdrawDTO();
        reqDTO.setCurrentPassword("1234");
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.createWithdrawFailurePage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()),
                "withdraw blocked");

        doThrow(new Exception403("withdraw blocked"))
                .when(userService)
                .withdrawAccount(9, "1234");
        when(mypageService.getWithdrawFailureMainPage(9, "withdraw blocked")).thenReturn(pageDTO);

        String view = controller.withdrawAccount(reqDTO, model);

        MypageResponse.MainPageDTO page = (MypageResponse.MainPageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("withdraw blocked", page.getWithdrawError());
        assertTrue(page.isWithdrawModalOpen());
        assertFalse(page.isPasswordModalOpen());
        verify(userService).withdrawAccount(9, "1234");
        verify(mypageService).getWithdrawFailureMainPage(9, "withdraw blocked");
    }

    @Test
    void wdOk() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(11, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();
        MypageRequest.WithdrawDTO reqDTO = new MypageRequest.WithdrawDTO();
        reqDTO.setCurrentPassword("1234");

        String view = controller.withdrawAccount(reqDTO, model);

        assertEquals("redirect:/login-form", view);
        assertTrue(session.isInvalid());
        verify(userService).withdrawAccount(11, "1234");
        verifyNoInteractions(mypageService);
    }

    @Test
    void booking() {
        MypageService mypageService = mock(MypageService.class);
        UserService userService = mock(UserService.class);
        MockHttpSession session = session(21, "USER");
        MypageController controller = new MypageController(mypageService, userService, session);
        Model model = new ExtendedModelMap();

        MypageResponse.BookingDetailPageDTO pageDTO = MypageResponse.BookingDetailPageDTO.fromBookingDetail(
                BookingResponse.BookingDetailDTO.builder()
                        .id(21)
                        .tripPlanId(3)
                        .lodgingName("Ocean View Hotel")
                        .roomName("Standard")
                        .location("Jeju")
                        .imageUrl("https://example.com/room.jpg")
                        .checkIn(LocalDate.of(2026, 4, 10))
                        .checkOut(LocalDate.of(2026, 4, 12))
                        .guestCount(2)
                        .pricePerNight(280000)
                        .taxAndServiceFee(50400)
                        .totalPriceText("330,400원")
                        .statusCode("booked")
                        .statusLabel("예약 확정")
                        .createdAt(LocalDateTime.of(2026, 3, 21, 10, 0))
                        .canCancel(true)
                        .build());

        when(mypageService.getBookingDetailPage(21, 21)).thenReturn(pageDTO);

        String view = controller.showBookingDetailPage(21, model);

        MypageResponse.BookingDetailPageDTO page = (MypageResponse.BookingDetailPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-detail", view);
        assertEquals(21, page.getBookingId());
        assertEquals("/mypage/bookings", page.getBookingListLink());
        assertEquals("/mypage", page.getMypageLink());
        assertEquals("예약 확정", page.getStatusLabel());
        assertEquals("330,400원", page.getTotalPriceText());
        assertEquals("/api/bookings/21", page.getCancelApiUrl());
        assertFalse(page.isCancelled());
        verify(mypageService).getBookingDetailPage(21, 21);
        verifyNoInteractions(userService);
    }

    @Test
    void booking404() {
        MypageService mypageService = mock(MypageService.class);
        MypageController controller = new MypageController(mypageService, mock(UserService.class), session(21, "USER"));

        when(mypageService.getBookingDetailPage(21, 999)).thenThrow(new Exception404("booking not found"));

        assertThrows(Exception404.class, () -> controller.showBookingDetailPage(999, new ExtendedModelMap()));
    }

    @Test
    void main401() {
        MypageController controller = new MypageController(mock(MypageService.class), mock(UserService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.showMainPage("", new ExtendedModelMap()));
    }

    @Test
    void list401() {
        MypageController controller = new MypageController(mock(MypageService.class), mock(UserService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.showBookingListPage(null, new ExtendedModelMap()));
    }

    @Test
    void booking401() {
        MypageController controller = new MypageController(mock(MypageService.class), mock(UserService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.showBookingDetailPage(21, new ExtendedModelMap()));
    }

    private MockHttpSession session(Integer userId, String role) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", role));
        return session;
    }
}
