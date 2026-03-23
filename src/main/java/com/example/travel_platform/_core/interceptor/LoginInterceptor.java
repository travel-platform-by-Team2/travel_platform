package com.example.travel_platform._core.interceptor;

import com.example.travel_platform._core.util.Script;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.UserSessionChecker;

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

    private final UserSessionChecker userSessionChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        SessionUser sessionUser = SessionUsers.getOrNull(session);

        if (isPublicBoardDetailRequest(request)) {
            return true;
        }

        if (sessionUser == null) {
            handleUnauthenticatedRequest(request, response);
            return false;
        }

        if (userSessionChecker.isBlocked(sessionUser)) {
            session.invalidate();
            handleBlockedUserRequest(request, response);
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
