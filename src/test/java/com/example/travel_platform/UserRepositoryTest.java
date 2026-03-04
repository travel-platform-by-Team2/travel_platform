package com.example.travel_platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

@Import(UserRepository.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void save_test() {
        // given
        User user = new User();
        user.setUsername("user1");
        user.setPassword("1234");
        user.setEmail("user1@metacoding.com");

        // when
        userRepository.save(user);
        // eye
        System.out.println("=======================");
        System.out.println("id : " + user.getId());
        System.out.println("username : " + user.getUsername());
        System.out.println("email : " + user.getEmail());
    }
}
