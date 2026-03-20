package com.example.travel_platform._core.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] ADMIN_ONLY_PATHS = {
            "/admin",
            "/admin/**"
    };

    private static final String[] LOGIN_REQUIRED_PATHS = {
            "/boards/*",
            "/boards/*/edit",
            "/boards/*/update",
            "/boards/*/delete",
            "/boards/*/replies",
            "/boards/*/replies/*/delete",
            "/api/boards/*/likes/toggle",
            "/api/boards/*/replies",
            "/api/boards/*/replies/*",
            "/calendar",
            "/api/calendar",
            "/api/calendar/**",
            "/trip",
            "/trip/**",
            "/api/trips",
            "/api/trips/**",
            "/mypage",
            "/mypage/**"
    };

    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(ADMIN_ONLY_PATHS);

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(LOGIN_REQUIRED_PATHS);
    }
}
