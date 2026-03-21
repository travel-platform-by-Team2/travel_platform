package com.example.travel_platform.user;

import com.example.travel_platform._core.handler.ex.Exception401;

import jakarta.servlet.http.HttpSession;

public final class SessionUsers {

    public static final String SESSION_USER_KEY = "sessionUser";

    private SessionUsers() {
    }

    public static void save(HttpSession session, SessionUser sessionUser) {
        session.setAttribute(SESSION_USER_KEY, sessionUser);
    }

    public static SessionUser getOrNull(HttpSession session) {
        Object sessionUser = session.getAttribute(SESSION_USER_KEY);

        if (sessionUser instanceof SessionUser value) {
            return value;
        }

        if (sessionUser instanceof User value) {
            SessionUser migrated = SessionUser.fromUser(value);
            save(session, migrated);
            return migrated;
        }

        return null;
    }

    public static SessionUser require(HttpSession session) {
        SessionUser sessionUser = getOrNull(session);
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser;
    }

    public static Integer getUserIdOrNull(HttpSession session) {
        SessionUser sessionUser = getOrNull(session);
        if (sessionUser == null) {
            return null;
        }
        return sessionUser.getId();
    }

    public static Integer requireUserId(HttpSession session) {
        return require(session).getId();
    }
}
