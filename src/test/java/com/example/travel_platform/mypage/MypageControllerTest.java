package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
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
        MypageResponse.PageDTO pageDTO = MypageResponse.PageDTO.builder().build();

        when(mypageService.getMainPage(3)).thenReturn(pageDTO);

        String view = controller.showMainPage("비밀번호가 변경되었습니다.", model);

        assertEquals("pages/mypage", view);
        assertSame(pageDTO, model.getAttribute("model"));
        assertEquals("비밀번호가 변경되었습니다.", pageDTO.getPasswordSuccessMessage());
        assertEquals(null, pageDTO.getPasswordError());
        assertEquals(null, pageDTO.getWithdrawError());
        assertFalse(pageDTO.isPasswordModalOpen());
        assertFalse(pageDTO.isWithdrawModalOpen());
        verify(mypageService).getMainPage(3);
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
        MypageResponse.PageDTO pageDTO = MypageResponse.PageDTO.builder().build();

        doThrow(new Exception400("현재 비밀번호가 일치하지 않습니다."))
                .when(mypageService)
                .changePassword(5, reqDTO);
        when(mypageService.getMainPage(5)).thenReturn(pageDTO);

        String view = controller.changePassword(reqDTO, model, redirectAttributes);

        MypageResponse.PageDTO page = (MypageResponse.PageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("현재 비밀번호가 일치하지 않습니다.", page.getPasswordError());
        assertTrue(page.isPasswordModalOpen());
        assertFalse(page.isWithdrawModalOpen());
        verify(mypageService).changePassword(5, reqDTO);
        verify(mypageService).getMainPage(5);
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
        assertEquals("비밀번호가 변경되었습니다.", redirectAttributes.getFlashAttributes().get("passwordSuccessMessage"));
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
        MypageResponse.PageDTO pageDTO = MypageResponse.PageDTO.builder().build();

        doThrow(new Exception403("관리자 계정은 탈퇴할 수 없습니다."))
                .when(userService)
                .withdrawAccount(9, "1234");
        when(mypageService.getMainPage(9)).thenReturn(pageDTO);

        String view = controller.withdrawAccount(reqDTO, model);

        MypageResponse.PageDTO page = (MypageResponse.PageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("관리자 계정은 탈퇴할 수 없습니다.", page.getWithdrawError());
        assertTrue(page.isWithdrawModalOpen());
        assertFalse(page.isPasswordModalOpen());
        verify(userService).withdrawAccount(9, "1234");
        verify(mypageService).getMainPage(9);
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

        MypageResponse.BookingDetailPageDTO pageDTO = MypageResponse.BookingDetailPageDTO.createBookingDetailPage(21);

        when(mypageService.getBookingDetailPage(21, 21)).thenReturn(pageDTO);

        String view = controller.showBookingDetailPage(21, model);

        MypageResponse.BookingDetailPageDTO page = (MypageResponse.BookingDetailPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-detail", view);
        assertEquals(21, page.getBookingId());
        assertEquals("/mypage", page.getBackLink());
        assertEquals("현재 화면은 placeholder이며 예약 ID만 연결된 상태입니다.", page.getPlaceholderNotice());
        verify(mypageService).getBookingDetailPage(21, 21);
        verifyNoInteractions(userService);
    }

    @Test
    void main401() {
        MypageController controller = new MypageController(mock(MypageService.class), mock(UserService.class), new MockHttpSession());

        assertThrows(Exception401.class, () -> controller.showMainPage("", new ExtendedModelMap()));
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
