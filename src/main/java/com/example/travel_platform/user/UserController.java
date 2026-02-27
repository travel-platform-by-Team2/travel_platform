package com.example.travel_platform.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final HttpSession session;

    @GetMapping("/join-form")
    public String joinForm() {
        return "user/join-form";
    }

    @PostMapping("/join")
    public String join(UserRequest.SaveDTO requestDTO) {
        userService.회원가입(requestDTO);
        return "redirect:/login-form";
    }

    @GetMapping("/login-form")
    public String loginForm() {
        return "/login-form";
    }

    @PostMapping("/login")
    public String login(UserRequest.LoginDTO requestDTO) {
        User user = userService.로그인(requestDTO);
        session.setAttribute("session", user); // 세션에 저장
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 세션 종료
        return "redirect:/";
    }
}