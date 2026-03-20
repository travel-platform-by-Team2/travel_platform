package com.example.travel_platform.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final HttpSession session;

    @GetMapping("/")
    public String mainIndex() {
        return "pages/main-index";
    }

    // 로그아웃 시 리다이렉션 경로를 / (메인 페이지)로 변경 완료(지윤)
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }

    // 로그인 성공 : 메인 페이지(/)로 리다이렉트 : 지윤
    @PostMapping("/login")
    public String login(UserRequest.LoginDTO reqDTO) {
        SessionUsers.save(session, userService.login(reqDTO));
        return "redirect:/";
    }

    // SNS 로그인 콜백 (카카오/네이버/구글 공통)
    @GetMapping("/auth/{provider}/callback")
    public String snsCallback(@PathVariable("provider") String provider, @RequestParam("code") String code) {
        try {
            // 실제 구현 시 OAuthService를 통해 토큰 및 유저 정보를 가져와야 함
            // 여기서는 흐름 확인을 위한 가상 데이터를 사용
            String email = "test_" + provider + "@example.com";
            
            // 제공자별 한글 이름 설정
            String nickname = provider.equals("kakao") ? "카카오" : 
                             provider.equals("naver") ? "네이버" : "구글";
            
            String providerId = "12345"; 

            SessionUser sessionUser = userService.snsLogin(email, nickname, provider, providerId);
            SessionUsers.save(session, sessionUser);

            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 회원가입 성공_가입 후 로그인 폼으로 이동 : 지윤
    @PostMapping("/join")
    public String join(@Valid UserRequest.JoinDTO reqDTO, Errors errors) {
        if (errors.hasErrors()) {
            return "pages/signup";
        }
        userService.join(reqDTO);
        return "redirect:/login-form";
    }

    @GetMapping("/login-form")
    public String loginForm() {
        return "pages/login";
    }

    @GetMapping("/join-form")
    public String joinForm() {
        return "pages/signup";
    }

}
