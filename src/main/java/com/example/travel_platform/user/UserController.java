package com.example.travel_platform.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.example.travel_platform._core.handler.ex.Exception401;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final String MODEL = "model";
    private static final String REDIRECT_HOME = "redirect:/";
    private static final String REDIRECT_LOGIN_FORM = "redirect:/login-form";

    private final UserService userService;
    private final UserSessionManager userSessionManager;

    @Value("${KAKAO_JS_APP_KEY:}")
    private String kakaoJsAppKey;

    @Value("${NAVER_CLIENT_ID:}")
    private String naverClientId;

    @Value("${NAVER_CLIENT_SECRET:}")
    private String naverClientSecret;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @GetMapping("/")
    public String mainIndex() {
        return "pages/main-index";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        userSessionManager.signOut(request.getSession(false));
        return REDIRECT_HOME;
    }

    @PostMapping("/login")
    public String login(UserRequest.LoginDTO reqDTO, HttpServletRequest request) {
        userSessionManager.signIn(request, userService.login(reqDTO));
        return REDIRECT_HOME;
    }

    /**
     * 네이버 로그인 콜백 (인가 코드 방식)
     * 주소창에 토큰이 노출되지 않도록 서버에서 처리합니다.
     */
    @GetMapping("/auth/naver/callback")
    public String naverCallback(@RequestParam(name = "code") String code,
                               @RequestParam(name = "state") String state,
                               HttpServletRequest request) {
        
        // [디버그] .env에서 값을 잘 읽어왔는지 확인 (보안을 위해 일부만 출력)
        System.out.println("=== 네이버 로그인 시도 ===");
        System.out.println("ID: " + (naverClientId != null && naverClientId.length() > 5 ? naverClientId.substring(0, 5) + "****" : "null/short"));
        System.out.println("Secret: " + (naverClientSecret != null && naverClientSecret.length() > 3 ? naverClientSecret.substring(0, 3) + "****" : "null/short"));

        RestTemplate rt = new RestTemplate();

        try {
            // 1. 인가 코드로 엑세스 토큰 요청 (서버 간 통신)
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code" +
                    "&client_id=" + naverClientId +
                    "&client_secret=" + naverClientSecret +
                    "&code=" + code +
                    "&state=" + state;

            ResponseEntity<Map> tokenResponseEntity = rt.getForEntity(tokenUrl, Map.class);
            Map<String, Object> tokenResponse = tokenResponseEntity.getBody();
            
            if (tokenResponse == null || tokenResponse.get("access_token") == null) {
                System.err.println("네이버 토큰 응답 오류: " + tokenResponse);
                throw new Exception401("네이버 인증 토큰을 가져오지 못했습니다.");
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // 2. 엑세스 토큰으로 사용자 정보 요청
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> profileResponse = rt.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> body = profileResponse.getBody();
            Map<String, String> response = (Map<String, String>) body.get("response");

            if (response == null) {
                System.err.println("네이버 사용자 정보 응답 오류: " + body);
                throw new Exception401("네이버 사용자 정보를 가져오지 못했습니다.");
            }

            String providerId = response.get("id");
            String nickname = response.get("nickname");
            String email = response.get("email");

            // 3. 로그인 처리 (우리 서비스의 세션 방식 유지)
            SessionUser sessionUser = userService.loginWithSns(email, nickname, "naver", providerId);
            userSessionManager.signIn(request, sessionUser);

            return REDIRECT_HOME;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("=== 네이버 API 통신 에러 (HTTP 4xx) ===");
            System.err.println("상태 코드: " + e.getStatusCode());
            System.err.println("에러 본문: " + e.getResponseBodyAsString());
            throw new Exception401("네이버 인증 실패: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("=== 네이버 로그인 중 예외 발생 ===");
            e.printStackTrace();
            throw e;
        }
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
        userSessionManager.signIn(request, sessionUser);
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
}
