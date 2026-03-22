package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform._core.handler.ex.Exception401;

class SessionUsersTest {

    @Test
    void migrate() {
        MockHttpSession session = new MockHttpSession();
        User user = User.create("cos", "1234", "cos@nate.com", "010-5555-6666", "USER");
        user.setId(2);
        session.setAttribute(SessionUsers.SESSION_USER_KEY, user);

        SessionUser sessionUser = SessionUsers.getOrNull(session);

        assertEquals(2, sessionUser.getId());
        assertEquals("cos", sessionUser.getUsername());
        assertInstanceOf(SessionUser.class, session.getAttribute(SessionUsers.SESSION_USER_KEY));
    }

    @Test
    void needLogin() {
        MockHttpSession session = new MockHttpSession();

        Exception401 exception = assertThrows(Exception401.class, () -> SessionUsers.requireUserId(session));

        assertEquals("로그인이 필요합니다.", exception.getMessage());
    }
}
