package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void snsLogin_신규유저_가입_테스트() {
        // given
        String email = "new_sns_user@example.com";
        String username = "SNSUser";
        String provider = "kakao";
        String providerId = "12345678";

        // when
        SessionUser result = userService.snsLogin(email, username, provider, providerId);

        // then
        assertNotNull(result.getId());
        assertEquals(email, result.getEmail());
        
        Optional<User> userOP = userRepository.findByEmailAndProvider(email, provider);
        assertEquals(true, userOP.isPresent());
        assertEquals(provider, userOP.get().getProvider());
    }

    @Test
    public void snsLogin_기존유저_로그인_테스트() {
        // given
        String email = "existing_sns_user@example.com";
        String username = "OldUser";
        String provider = "google";
        String providerId = "87654321";
        
        // 미리 유저 저장
        User existingUser = User.createSNS(username, email, provider, providerId);
        userRepository.save(existingUser);

        // when
        SessionUser result = userService.snsLogin(email, username, provider, providerId);

        // then
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(email, result.getEmail());
    }
}
