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
import com.example.travel_platform._core.handler.ex.Exception404;
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
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.builder().build();

        when(mypageService.getMainPage(3)).thenReturn(pageDTO);

        String view = controller.showMainPage("鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.", model);

        assertEquals("pages/mypage", view);
        assertSame(pageDTO, model.getAttribute("model"));
        assertEquals("鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.", pageDTO.getPasswordSuccessMessage());
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
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.builder().build();

        doThrow(new Exception400("?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎."))
                .when(mypageService)
                .changePassword(5, reqDTO);
        when(mypageService.getMainPage(5)).thenReturn(pageDTO);

        String view = controller.changePassword(reqDTO, model, redirectAttributes);

        MypageResponse.MainPageDTO page = (MypageResponse.MainPageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.", page.getPasswordError());
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
        Object successMessage = redirectAttributes.getFlashAttributes().get("passwordSuccessMessage");
        assertTrue(successMessage instanceof String);
        assertFalse(((String) successMessage).isBlank());
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
        MypageResponse.MainPageDTO pageDTO = MypageResponse.MainPageDTO.builder().build();

        doThrow(new Exception403("愿由ъ옄 怨꾩젙? ?덊눜?????놁뒿?덈떎."))
                .when(userService)
                .withdrawAccount(9, "1234");
        when(mypageService.getMainPage(9)).thenReturn(pageDTO);

        String view = controller.withdrawAccount(reqDTO, model);

        MypageResponse.MainPageDTO page = (MypageResponse.MainPageDTO) model.getAttribute("model");
        assertEquals("pages/mypage", view);
        assertSame(pageDTO, page);
        assertEquals("愿由ъ옄 怨꾩젙? ?덊눜?????놁뒿?덈떎.", page.getWithdrawError());
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

        MypageResponse.BookingDetailPlaceholderPageDTO pageDTO = MypageResponse.BookingDetailPlaceholderPageDTO
                .createBookingDetailPlaceholderPage(21);

        when(mypageService.getBookingDetailPage(21, 21)).thenReturn(pageDTO);

        String view = controller.showBookingDetailPage(21, model);

        MypageResponse.BookingDetailPlaceholderPageDTO page = (MypageResponse.BookingDetailPlaceholderPageDTO) model
                .getAttribute("model");
        assertEquals("pages/booking-detail", view);
        assertEquals(21, page.getBookingId());
        assertEquals("/mypage", page.getBackLink());
        assertEquals("?꾩옱 ?붾㈃? placeholder?대ŉ ?덉빟 ID留??곌껐???곹깭?낅땲??", page.getPlaceholderNotice());
        verify(mypageService).getBookingDetailPage(21, 21);
        verifyNoInteractions(userService);
    }

    @Test
    void booking404() {
        MypageService mypageService = mock(MypageService.class);
        MypageController controller = new MypageController(mypageService, mock(UserService.class), session(21, "USER"));

        when(mypageService.getBookingDetailPage(21, 999)).thenThrow(new Exception404("?덉빟 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));

        assertThrows(Exception404.class, () -> controller.showBookingDetailPage(999, new ExtendedModelMap()));
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
