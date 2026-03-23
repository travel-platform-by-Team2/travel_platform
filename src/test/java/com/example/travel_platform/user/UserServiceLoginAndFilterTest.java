package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.interceptor.AdminInterceptor;
import com.example.travel_platform._core.interceptor.LoginInterceptor;

@SpringBootTest
@Transactional
class UserServiceLoginAndFilterTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionChecker userSessionChecker;

    @Test
    void loginOff() {
        UserRequest.LoginDTO reqDTO = new UserRequest.LoginDTO();
        reqDTO.setEmail("cos@nate.com");
        reqDTO.setPassword("1234");

        Exception403 exception = assertThrows(Exception403.class, () -> userService.login(reqDTO));
        assertEquals("현재 로그인할 수 없는 계정입니다", exception.getMessage());
    }

    @Test
    void loginPage() throws Exception {
        LoginInterceptor loginInterceptor = new LoginInterceptor(userSessionChecker);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mypage");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = inactiveSession();
        MockFilterChain chain = new MockFilterChain();

        request.setSession(session);

        loginInterceptor.preHandle(request, response, chain);

        assertTrue(response.getContentAsString().contains("계정 상태가 변경되어 다시 로그인해 주세요"));
        assertTrue(response.getContentAsString().contains("/login-form"));
        assertTrue(session.isInvalid());
    }

    @Test
    void loginApi() throws Exception {
        LoginInterceptor loginInterceptor = new LoginInterceptor(userSessionChecker);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/calendar");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = inactiveSession();
        MockFilterChain chain = new MockFilterChain();

        request.setSession(session);

        loginInterceptor.preHandle(request, response, chain);

        assertEquals(403, response.getStatus());
        assertTrue(session.isInvalid());
    }

    @Test
    void adminPage() throws Exception {
        AdminInterceptor adminInterceptor = new AdminInterceptor(userSessionChecker);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = inactiveSession();
        MockFilterChain chain = new MockFilterChain();

        request.setSession(session);

        adminInterceptor.preHandle(request, response, chain);

        assertTrue(response.getContentAsString().contains("계정 상태가 변경되어 다시 로그인해 주세요"));
        assertTrue(response.getContentAsString().contains("/login-form"));
        assertTrue(session.isInvalid());
    }

    private MockHttpSession inactiveSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(2, "cos", "cos@nate.com", "010-5555-6666", "USER"));
        return session;
    }
}
