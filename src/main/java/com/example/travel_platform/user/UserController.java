package com.example.travel_platform.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        // session.removeAttribute("sessionUser");
        return "redirect:/";
    }

    // 조회인데, 예외로 post 요청
    @PostMapping("/login")
    public String login(@Valid UserRequest.LoginDTO reqDTO, Errors errors, HttpServletResponse resp) {
        User sessionUser = userService.login(reqDTO.getEmail(), reqDTO.getPassword());
        session.setAttribute("sessionUser", sessionUser);

        return "redirect:/main-index";
    }

    @PostMapping("/join")
    public String join(@Valid UserRequest.JoinDTO reqDTO, Errors errors) {
        userService.join(reqDTO.getUsername(), reqDTO.getPassword(), reqDTO.getEmail());

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
