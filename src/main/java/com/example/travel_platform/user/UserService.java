package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void 회원가입(String username, String password, String email) {
        // 1. 유저네임 중복 체크 (필터링)!!
        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            throw new Exception400("유저 네임이 중복되었습니다!!!");
        }

        // 2. 비영속 객체
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // 3. save() 호출
        userRepository.save(user);

    }

    public User 로그인(String username, String password) {
        User findUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception400("username을 찾을 수가 없어요"));

        if (!findUser.getPassword().equals(password)) {
            throw new Exception401("패스워드가 일치하지 않아요");
        }

        return findUser;
    }

}
