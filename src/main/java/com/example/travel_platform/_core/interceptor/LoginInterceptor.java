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
        String uri = request.getRequestURI();

        if (isPublicBoardDetailRequest(request, uri)) {
            return true;
        }

        if (sessionUser == null) {
            if (uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            response.sendRedirect("/login-form");
            return false;
        }

        if (userSessionChecker.isBlocked(sessionUser)) {
            session.invalidate();
            handleBlockedUserResponse(request, response);
            return false;
        }

        return true;
    }

    private boolean isPublicBoardDetailRequest(HttpServletRequest request, String uri) {
        return "GET".equals(request.getMethod()) && uri.matches(".*/boards/\\d+$");
    }

    private void handleBlockedUserResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("text/html; charset=utf-8");
        response.getWriter().println(Script.href("/login-form", SESSION_BLOCKED_MESSAGE));
    }
}
