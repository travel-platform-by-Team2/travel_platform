package com.example.travel_platform._core.filter;

import java.io.IOException;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("------Admin Filter-----------");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        SessionUser sessionUser = SessionUsers.getOrNull(session);

        // 1. 로그인을 안 했거나 2. 권한이 "ADMIN"이 아니면 입구 컷!
        if (sessionUser == null || !sessionUser.isAdmin()) {
            resp.setContentType("text/html; charset=utf-8");
            resp.getWriter().println("<script>alert('관리자 권한이 필요합니다'); location.href='/';</script>");
            return;
        }

        chain.doFilter(request, response);
    }

}
