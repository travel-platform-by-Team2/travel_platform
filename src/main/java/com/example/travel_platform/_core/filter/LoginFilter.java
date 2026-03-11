package com.example.travel_platform._core.filter;

import java.io.IOException;

import com.example.travel_platform.user.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("------Login Filter-----------");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 예외: GET /boards/{id}는 로그인 없이도 조회
        String uri = req.getRequestURI();
        if ("GET".equals(req.getMethod()) && uri.matches(".*/boards/\\d+$")) {
            chain.doFilter(request, response);
            return;
        }

        // 로그인 필터 적용 대상
        if (sessionUser == null) {
            if (uri.startsWith("/api/")) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            resp.sendRedirect("/login-form");
            return;
        }

        chain.doFilter(request, response);

    }

}
