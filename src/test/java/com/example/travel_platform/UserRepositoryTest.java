package com.example.travel_platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

@Import(UserRepository.class)
@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void save_test() {
        // given
        User user = User.createSNS("user1", "user1@metacoding.com", "kakao", "provider-1");

        // when
        userRepository.save(user);
        // eye
        System.out.println("=======================");
        System.out.println("id : " + user.getId());
        System.out.println("username : " + user.getUsername());
        System.out.println("email : " + user.getEmail());
    }
}
