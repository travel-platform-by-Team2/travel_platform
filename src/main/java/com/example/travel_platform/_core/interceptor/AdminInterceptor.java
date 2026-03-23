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
public class AdminInterceptor implements HandlerInterceptor {

    private static final String SESSION_BLOCKED_MESSAGE = "계정 상태가 변경되어 다시 로그인해 주세요.";

    private final UserSessionChecker userSessionChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        SessionUser sessionUser = SessionUsers.getOrNull(session);

        if (userSessionChecker.isBlocked(sessionUser)) {
            session.invalidate();
            writeScriptResponse(response, Script.href("/login-form", SESSION_BLOCKED_MESSAGE));
            return false;
        }

        if (sessionUser == null || !sessionUser.isAdmin()) {
            writeScriptResponse(response, Script.href("/", "관리자 권한이 필요합니다"));
            return false;
        }

        return true;
    }

    private void writeScriptResponse(HttpServletResponse response, String script) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        response.getWriter().println(script);
    }
}
