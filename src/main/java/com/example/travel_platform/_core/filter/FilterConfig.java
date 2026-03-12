package com.example.travel_platform._core.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter() {
        FilterRegistrationBean<LoginFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new LoginFilter());
        bean.addUrlPatterns("/boards/*", "/calendar", "/calendar/*", "/api/calendar", "/api/calendar/*");

        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilter() {
        FilterRegistrationBean<AdminFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AdminFilter());
        bean.addUrlPatterns("/admin/*"); // "/admin/"으로 시작하는 모든 경로는 이 필터를 거칩니다.
        bean.setOrder(1); // LoginFilter보다 먼저 실행되게 하거나 순서를 조정합니다.
        return bean;
    }
}
