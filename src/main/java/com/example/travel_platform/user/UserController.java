package com.example.travel_platform.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Value("${KAKAO_JS_APP_KEY:}")
    private String kakaoJsAppKey;

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

    // SNS 로그인 콜백 (프론트엔드에서 사용자 정보를 직접 전달받음)
    @GetMapping("/auth/{provider}/callback")
    public String snsCallback(
            @PathVariable("provider") String provider,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "providerId", required = false) String providerId,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 정보가 부족한 경우 예외 처리
            if (providerId == null || email == null) {
                return "redirect:/login-form?error=sns";
            }

            SessionUser sessionUser = userService.snsLogin(email, nickname, provider, providerId);
            
            // 기존 세션 무효화 후 새 세션 생성 (보안 및 세션 꼬임 방지)
            session.invalidate();
            HttpSession newSession = request.getSession(true);
            SessionUsers.save(newSession, sessionUser);

            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login-form?error=sns";
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
    public String loginForm(Model model) {
        model.addAttribute("kakaoJsAppKey", kakaoJsAppKey);
        return "pages/login";
    }

    @GetMapping("/join-form")
    public String joinForm() {
        return "pages/signup";
    }

}
