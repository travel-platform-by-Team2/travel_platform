package com.example.travel_platform._core.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin", "/admin/*");

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/boards/*",
                        "/calendar",
                        "/calendar/*",
                        "/api/calendar",
                        "/api/calendar/*",
                        "/trip",
                        "/trip/*",
                        "/api/trips",
                        "/api/trips/*",
                        "/mypage",
                        "/mypage/*");
    }
}
