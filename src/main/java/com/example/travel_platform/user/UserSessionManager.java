package com.example.travel_platform.user;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionManager {

    private final UserSessionRegistry userSessionRegistry;

    @Value("${server.servlet.session.timeout:30m}")
    private Duration sessionTimeout;

    public void signIn(HttpServletRequest request, SessionUser sessionUser) {
        invalidateCurrentSession(request);

        HttpSession newSession = request.getSession(true);
        newSession.setMaxInactiveInterval((int) sessionTimeout.getSeconds());
        SessionUsers.save(newSession, sessionUser);
        userSessionRegistry.register(sessionUser.getId(), newSession.getId());
    }

    public void signOut(HttpSession session) {
        if (session == null) {
            return;
        }

        SessionUser sessionUser = SessionUsers.getOrNull(session);
        String sessionId = session.getId();

        invalidateSession(session);

        if (sessionUser != null) {
            userSessionRegistry.remove(sessionUser.getId(), sessionId);
        }
    }

    private void invalidateCurrentSession(HttpServletRequest request) {
        HttpSession currentSession = request.getSession(false);
        if (currentSession == null) {
            return;
        }

        signOut(currentSession);
    }

    private void invalidateSession(HttpSession session) {
        session.invalidate();
    }
}
