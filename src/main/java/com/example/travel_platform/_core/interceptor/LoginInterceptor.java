package com.example.travel_platform._core.interceptor;

import com.example.travel_platform._core.util.Script;
import com.example.travel_platform.user.UserSessionPolicyChecker;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String SESSION_BLOCKED_MESSAGE = "계정 상태가 변경되어 다시 로그인해 주세요.";
    private static final String CONCURRENT_LOGOUT_REDIRECT_URL = "/login-form?forcedLogout=concurrent";
    private static final String CONCURRENT_LOGOUT_HEADER = "X-Session-Status";
    private static final String CONCURRENT_LOGOUT_HEADER_VALUE = "concurrent-login";

    private final UserSessionPolicyChecker userSessionPolicyChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserSessionPolicyChecker.SessionValidation validation = userSessionPolicyChecker.validate(session);

        if (isPublicBoardDetailRequest(request)) {
            return true;
        }

        if (validation.isUnauthenticated()) {
            handleUnauthenticatedRequest(request, response);
            return false;
        }

        if (validation.isBlocked()) {
            session.invalidate();
            handleBlockedUserRequest(request, response);
            return false;
        }

        if (validation.isConcurrentlyLoggedOut()) {
            session.invalidate();
            handleConcurrentLogoutRequest(request, response);
            return false;
        }

        return true;
    }

    private boolean isPublicBoardDetailRequest(HttpServletRequest request) {
        return isGetRequest(request) && request.getRequestURI().matches(".*/boards/\\d+$");
    }

    private boolean isGetRequest(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

    private void handleUnauthenticatedRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isApiRequest(request)) {
            writeStatus(response, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.sendRedirect("/login-form");
    }

    private void handleBlockedUserRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isApiRequest(request)) {
            writeStatus(response, HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        writeScriptResponse(response, Script.href("/login-form", SESSION_BLOCKED_MESSAGE));
    }

    private void handleConcurrentLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isApiRequest(request)) {
            response.setHeader(CONCURRENT_LOGOUT_HEADER, CONCURRENT_LOGOUT_HEADER_VALUE);
            writeStatus(response, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.sendRedirect(CONCURRENT_LOGOUT_REDIRECT_URL);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private void writeStatus(HttpServletResponse response, int status) {
        response.setStatus(status);
    }

    private void writeScriptResponse(HttpServletResponse response, String script) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        response.getWriter().println(script);
    }
}
