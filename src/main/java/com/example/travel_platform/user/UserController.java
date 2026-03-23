package com.example.travel_platform.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform._core.handler.ex.Exception401;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final String MODEL = "model";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_LOGIN_FORM = "redirect:/login-form";

    private final UserService userService;
    private final HttpSession session;

    @Value("${KAKAO_JS_APP_KEY:}")
    private String kakaoJsAppKey;

    @Value("${NAVER_CLIENT_ID:}")
    private String naverClientId;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

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
    public String snsCallback(
            @PathVariable(name = "provider") String provider,
            UserRequest.SnsCallbackDTO reqDTO,
            HttpServletRequest request) {
        if (reqDTO.getProviderId() == null || reqDTO.getEmail() == null) {
            throw new Exception401("SNS 로그인 정보가 올바르지 않습니다.");
        }

        SessionUser sessionUser = userService.loginWithSns(
                reqDTO.getEmail(),
                reqDTO.getNickname(),
                provider,
                reqDTO.getProviderId());
        renewSession(request, sessionUser);
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
    public String loginForm(Model model) {
        model.addAttribute(MODEL, createLoginPageModel());
        return "pages/login";
    }

    @GetMapping("/join-form")
    public String joinForm() {
        return "pages/signup";
    }

    private UserResponse.LoginPageModelDTO createLoginPageModel() {
        return UserResponse.LoginPageModelDTO.createLoginPageModel(
                kakaoJsAppKey,
                naverClientId,
                googleClientId);
    }

    private void signIn(SessionUser sessionUser) {
        SessionUsers.save(session, sessionUser);
    }

    private void clearSession() {
        session.invalidate();
    }

    private void renewSession(HttpServletRequest request, SessionUser sessionUser) {
        clearSession();
        HttpSession newSession = request.getSession(true);
        SessionUsers.save(newSession, sessionUser);
    }
}
