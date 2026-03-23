package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

class UserSessionManagerTest {

    @Test
    void signInRenewsSessionAndRegistersRegistry() {
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        UserSessionManager manager = new UserSessionManager(registry);
        ReflectionTestUtils.setField(manager, "sessionTimeout", java.time.Duration.ofMinutes(30));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession previousSession = new MockHttpSession();
        SessionUsers.save(previousSession, new SessionUser(99, "old", "old@test.com", "010", "USER"));
        request.setSession(previousSession);

        SessionUser sessionUser = new SessionUser(1, "cos", "cos@test.com", "010", "USER");

        manager.signIn(request, sessionUser);

        MockHttpSession renewedSession = (MockHttpSession) request.getSession(false);
        assertTrue(previousSession.isInvalid());
        assertSame(sessionUser, renewedSession.getAttribute(SessionUsers.SESSION_USER_KEY));
        assertEquals(1800, renewedSession.getMaxInactiveInterval());
        verify(registry).remove(99, previousSession.getId());
        verify(registry).register(1, renewedSession.getId());
    }

    @Test
    void signOutRemovesRegistryForCurrentSession() {
        UserSessionRegistry registry = mock(UserSessionRegistry.class);
        UserSessionManager manager = new UserSessionManager(registry);
        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser(2, "neo", "neo@test.com", "010", "USER");
        SessionUsers.save(session, sessionUser);

        manager.signOut(session);

        assertTrue(session.isInvalid());
        verify(registry).remove(2, session.getId());
    }
}
