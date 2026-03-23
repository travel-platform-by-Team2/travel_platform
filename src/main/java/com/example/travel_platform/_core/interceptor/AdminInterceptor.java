package com.example.travel_platform._core.interceptor;

import com.example.travel_platform._core.util.Script;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.UserSessionPolicyChecker;

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
    private static final String CONCURRENT_LOGOUT_REDIRECT_URL = "/login-form?forcedLogout=concurrent";

    private final UserSessionPolicyChecker userSessionPolicyChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        UserSessionPolicyChecker.SessionValidation validation = userSessionPolicyChecker.validate(session);

        if (validation.isBlocked()) {
            session.invalidate();
            writeScriptResponse(response, Script.href("/login-form", SESSION_BLOCKED_MESSAGE));
            return false;
        }

        if (validation.isConcurrentlyLoggedOut()) {
            session.invalidate();
            response.sendRedirect(CONCURRENT_LOGOUT_REDIRECT_URL);
            return false;
        }

        SessionUser sessionUser = validation.getSessionUser();
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
