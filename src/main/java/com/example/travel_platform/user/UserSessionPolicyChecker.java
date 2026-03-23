package com.example.travel_platform.user;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionPolicyChecker {

    private final UserSessionChecker userSessionChecker;
    private final UserSessionRegistry userSessionRegistry;

    public SessionValidation validate(HttpSession session) {
        SessionUser sessionUser = SessionUsers.getOrNull(session);
        if (sessionUser == null) {
            return SessionValidation.unauthenticated();
        }

        if (userSessionChecker.isBlocked(sessionUser)) {
            return SessionValidation.blocked(sessionUser);
        }

        if (!userSessionRegistry.isCurrentSession(sessionUser.getId(), session.getId())) {
            return SessionValidation.concurrentlyLoggedOut(sessionUser);
        }

        return SessionValidation.authenticated(sessionUser);
    }

    public enum SessionState {
        UNAUTHENTICATED,
        AUTHENTICATED,
        BLOCKED,
        CONCURRENTLY_LOGGED_OUT
    }

    public record SessionValidation(SessionState state, SessionUser sessionUser) {

        public static SessionValidation unauthenticated() {
            return new SessionValidation(SessionState.UNAUTHENTICATED, null);
        }

        public static SessionValidation authenticated(SessionUser sessionUser) {
            return new SessionValidation(SessionState.AUTHENTICATED, sessionUser);
        }

        public static SessionValidation blocked(SessionUser sessionUser) {
            return new SessionValidation(SessionState.BLOCKED, sessionUser);
        }

        public static SessionValidation concurrentlyLoggedOut(SessionUser sessionUser) {
            return new SessionValidation(SessionState.CONCURRENTLY_LOGGED_OUT, sessionUser);
        }

        public boolean isUnauthenticated() {
            return state == SessionState.UNAUTHENTICATED;
        }

        public boolean isBlocked() {
            return state == SessionState.BLOCKED;
        }

        public boolean isConcurrentlyLoggedOut() {
            return state == SessionState.CONCURRENTLY_LOGGED_OUT;
        }
    }
}
