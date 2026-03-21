package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

class UserControllerTest {

    @Test
    void loginForm() {
        UserService userService = mock(UserService.class);
        MockHttpSession session = new MockHttpSession();
        UserController controller = new UserController(userService, session);
        ReflectionTestUtils.setField(controller, "kakaoJsAppKey", "kakao-key");
        ReflectionTestUtils.setField(controller, "naverClientId", "naver-id");
        ReflectionTestUtils.setField(controller, "googleClientId", "google-id");
        Model model = new ConcurrentModel();

        String viewName = controller.loginForm(model);

        assertEquals("pages/login", viewName);
        UserResponse.LoginPageModelDTO pageModel =
                (UserResponse.LoginPageModelDTO) model.getAttribute("model");
        assertEquals("kakao-key", pageModel.getKakaoJsAppKey());
        assertEquals("naver-id", pageModel.getNaverClientId());
        assertEquals("google-id", pageModel.getGoogleClientId());
    }

    @Test
    void snsOk() {
        UserService userService = mock(UserService.class);
        MockHttpSession session = new MockHttpSession();
        UserController controller = new UserController(userService, session);
        UserRequest.SnsCallbackDTO reqDTO = new UserRequest.SnsCallbackDTO();
        reqDTO.setEmail("sns@test.com");
        reqDTO.setNickname("sns");
        reqDTO.setProviderId("provider-id");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        SessionUser sessionUser = new SessionUser(7, "sns", "sns@test.com", "010", "USER");
        when(userService.loginWithSns("sns@test.com", "sns", "google", "provider-id"))
                .thenReturn(sessionUser);

        String viewName = controller.snsCallback("google", reqDTO, request);

        assertEquals("redirect:/", viewName);
        MockHttpSession renewedSession = (MockHttpSession) request.getSession(false);
        assertTrue(session.isInvalid());
        assertSame(sessionUser, renewedSession.getAttribute(SessionUsers.SESSION_USER_KEY));
    }

    @Test
    void snsFail() {
        UserService userService = mock(UserService.class);
        MockHttpSession session = new MockHttpSession();
        UserController controller = new UserController(userService, session);
        UserRequest.SnsCallbackDTO reqDTO = new UserRequest.SnsCallbackDTO();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String viewName = controller.snsCallback("kakao", reqDTO, request);

        assertEquals("redirect:/login-form?error=sns", viewName);
    }
}
