package com.example.travel_platform.user;

import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_LOGIN_FORM = "redirect:/login-form";

    private final UserService userService;
    private final HttpSession session;

    @GetMapping("/")
    public String mainIndex() {
        return "pages/main-index";
    }

    @GetMapping("/logout")
    public String logout() {
        clearSession();
        return REDIRECT_HOME;
    }

    @PostMapping("/login")
    public String login(UserRequest.LoginDTO reqDTO) {
        signIn(userService.login(reqDTO));
        return REDIRECT_HOME;
    }

    @GetMapping("/auth/{provider}/callback")
    public String snsCallback(@PathVariable String provider, String code) {
        signIn(createDemoSnsSessionUser(provider));
        return REDIRECT_HOME;
    }

    @PostMapping("/join")
    public String join(@Valid UserRequest.JoinDTO reqDTO, Errors errors) {
        if (errors.hasErrors()) {
            return "pages/signup";
        }
        userService.join(reqDTO);
        return REDIRECT_LOGIN_FORM;
    }

    @GetMapping("/login-form")
    public String loginForm() {
        return "pages/login";
    }

    @GetMapping("/join-form")
    public String joinForm() {
        return "pages/signup";
    }

    private void signIn(SessionUser sessionUser) {
        SessionUsers.save(session, sessionUser);
    }

    private void clearSession() {
        session.invalidate();
    }

    private SessionUser createDemoSnsSessionUser(String provider) {
        String email = "test_" + provider + "@example.com";
        String username = provider + "_User";
        String providerId = "unique_id_12345";
        return userService.snsLogin(email, username, provider, providerId);
    }
}
