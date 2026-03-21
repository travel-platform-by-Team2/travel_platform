package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserCodeTest {

    @Test
    void role() {
        assertEquals(UserRole.USER, UserRole.fromCode("USER"));
        assertEquals(UserRole.ADMIN, UserRole.fromCode("admin"));
        assertNull(UserRole.fromCodeOrNull(null));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromCode("GUEST"));
    }

    @Test
    void provider() {
        assertEquals(UserAuthProvider.KAKAO, UserAuthProvider.fromCode("kakao"));
        assertEquals(UserAuthProvider.GOOGLE, UserAuthProvider.fromCode("GOOGLE"));
        assertNull(UserAuthProvider.fromCodeOrNull(""));
        assertThrows(IllegalArgumentException.class, () -> UserAuthProvider.fromCode("github"));
    }

    @Test
    void sessionSnapshot() {
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "ADMIN");
        user.setId(1);

        SessionUser sessionUser = SessionUser.fromUserEntity(user);

        assertEquals(1, sessionUser.getId());
        assertEquals("ADMIN", sessionUser.getRole());
    }
}
