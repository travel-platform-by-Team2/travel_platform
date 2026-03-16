package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    // 회원가입 실패 : 중복 체크 후 예외발생 :지윤
    @Transactional
    public void join(String username, String password, String email, String tel) {
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
        user.setTel(tel);
        user.setRole("USER");
        // 3. save() 호출
        userRepository.save(user);

    }

    // 로그인 실패: 이메일/비밀번호 확인 후 예외 발생 (지윤)
    public User login(String email, String password) {

        User findUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception400("email을 찾을 수가 없어요"));

        if (!findUser.getPassword().equals(password)) {
            throw new Exception401("패스워드가 일치하지 않아요");
        }

        return findUser;
    }

}
