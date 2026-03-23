package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class UserSessionPolicyCheckerTest {

        @Test
        void unauthenticatedWhenSessionUserMissing() {
                UserSessionChecker sessionChecker = mock(UserSessionChecker.class);
                UserSessionRegistry registry = mock(UserSessionRegistry.class);
                UserSessionPolicyChecker checker = new UserSessionPolicyChecker(sessionChecker, registry);

                UserSessionPolicyChecker.SessionValidation validation = checker.validate(new MockHttpSession());

                assertEquals(UserSessionPolicyChecker.SessionState.UNAUTHENTICATED, validation.getState());
        }

        @Test
        void blockedWhenAccountStateChanged() {
                UserSessionChecker sessionChecker = mock(UserSessionChecker.class);
                UserSessionRegistry registry = mock(UserSessionRegistry.class);
                UserSessionPolicyChecker checker = new UserSessionPolicyChecker(sessionChecker, registry);
                MockHttpSession session = new MockHttpSession();
                SessionUser sessionUser = new SessionUser(1, "cos", "cos@test.com", "010", "USER");
                SessionUsers.save(session, sessionUser);
                when(sessionChecker.isBlocked(sessionUser)).thenReturn(true);

                UserSessionPolicyChecker.SessionValidation validation = checker.validate(session);

                assertEquals(UserSessionPolicyChecker.SessionState.BLOCKED, validation.getState());
        }

        @Test
        void concurrentlyLoggedOutWhenRegistrySessionDiffers() {
                UserSessionChecker sessionChecker = mock(UserSessionChecker.class);
                UserSessionRegistry registry = mock(UserSessionRegistry.class);
                UserSessionPolicyChecker checker = new UserSessionPolicyChecker(sessionChecker, registry);
                MockHttpSession session = new MockHttpSession();
                SessionUser sessionUser = new SessionUser(3, "neo", "neo@test.com", "010", "USER");
                SessionUsers.save(session, sessionUser);
                when(sessionChecker.isBlocked(sessionUser)).thenReturn(false);
                when(registry.isCurrentSession(3, session.getId())).thenReturn(false);

                UserSessionPolicyChecker.SessionValidation validation = checker.validate(session);

                assertEquals(UserSessionPolicyChecker.SessionState.CONCURRENTLY_LOGGED_OUT, validation.getState());
        }

        @Test
        void authenticatedWhenRegistrySessionMatches() {
                UserSessionChecker sessionChecker = mock(UserSessionChecker.class);
                UserSessionRegistry registry = mock(UserSessionRegistry.class);
                UserSessionPolicyChecker checker = new UserSessionPolicyChecker(sessionChecker, registry);
                MockHttpSession session = new MockHttpSession();
                SessionUser sessionUser = new SessionUser(7, "trinity", "tri@test.com", "010", "USER");
                SessionUsers.save(session, sessionUser);
                when(sessionChecker.isBlocked(sessionUser)).thenReturn(false);
                when(registry.isCurrentSession(7, session.getId())).thenReturn(true);

                UserSessionPolicyChecker.SessionValidation validation = checker.validate(session);

                assertEquals(UserSessionPolicyChecker.SessionState.AUTHENTICATED, validation.getState());
        }
}
