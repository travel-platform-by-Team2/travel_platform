package com.example.travel_platform._core.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.UserSessionPolicyChecker;
import com.example.travel_platform.user.UserSessionChecker;
import com.example.travel_platform.user.UserSessionRegistry;

class InterceptorBehaviorTest {

    @Test
    void loginApi401() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        LoginInterceptor interceptor = new LoginInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/boards/1/likes/toggle");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void loginPageRedirect() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        LoginInterceptor interceptor = new LoginInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mypage");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/login-form", response.getRedirectedUrl());
    }

    @Test
    void blockedApi403() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        LoginInterceptor interceptor = new LoginInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/trips/1/places");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionUsers.save(request.getSession(), new SessionUser(1, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));

        when(checker.isBlocked(SessionUsers.require(request.getSession()))).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void adminHome() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        AdminInterceptor interceptor = new AdminInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertTrue(response.getContentAsString().contains("관리자 권한이 필요합니다"));
        assertTrue(response.getContentAsString().contains("location.href='/'"));
    }

    @Test
    void adminBlocked() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        AdminInterceptor interceptor = new AdminInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionUsers.save(request.getSession(), new SessionUser(2, "cos", "cos@nate.com", "010-2222-3333", "USER"));

        when(checker.isBlocked(SessionUsers.require(request.getSession()))).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertTrue(response.getContentAsString().contains("/login-form"));
    }

    @Test
    void concurrentApi401WithHeader() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        LoginInterceptor interceptor = new LoginInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/calendar");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession();
        SessionUsers.save(session, new SessionUser(5, "neo", "neo@test.com", "010", "USER"));

        when(checker.isBlocked(SessionUsers.require(session))).thenReturn(false);
        when(registry.isCurrentSession(5, session.getId())).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertEquals("concurrent-login", response.getHeader("X-Session-Status"));
        assertTrue(session.isInvalid());
    }

    @Test
    void concurrentPageRedirectsWithForcedLogoutReason() throws Exception {
        UserSessionChecker checker = mock(UserSessionChecker.class);
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        LoginInterceptor interceptor = new LoginInterceptor(new UserSessionPolicyChecker(checker, registry));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mypage");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession();
        SessionUsers.save(session, new SessionUser(7, "trinity", "tri@test.com", "010", "USER"));

        when(checker.isBlocked(SessionUsers.require(session))).thenReturn(false);
        when(registry.isCurrentSession(7, session.getId())).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/login-form?forcedLogout=concurrent", response.getRedirectedUrl());
        assertTrue(session.isInvalid());
    }
}
