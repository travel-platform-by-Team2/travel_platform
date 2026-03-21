package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Test
    void snsJoin() {
        String email = "new_sns_user@example.com";
        String username = "SNSUser";
        String provider = "kakao";
        String providerId = "12345678";

        SessionUser result = userService.loginWithSns(email, username, provider, providerId);

        assertNotNull(result.getId());
        assertEquals(email, result.getEmail());

        Optional<User> user = userQueryRepository.findSnsUser(email, provider);
        assertTrue(user.isPresent());
        assertEquals(provider, user.get().getProvider());
    }

    @Test
    void snsReuse() {
        String email = "existing_sns_user@example.com";
        String username = "OldUser";
        String provider = "google";
        String providerId = "87654321";

        User existingUser = User.createSNS(username, email, provider, providerId);
        userRepository.save(existingUser);

        SessionUser result = userService.loginWithSns(email, username, provider, providerId);

        assertEquals(existingUser.getId(), result.getId());
        assertEquals(email, result.getEmail());
    }
}
