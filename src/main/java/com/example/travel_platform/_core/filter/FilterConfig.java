package com.example.travel_platform._core.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    // 로그인 필터
    // @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter() {
        FilterRegistrationBean<LoginFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new LoginFilter());
        bean.addUrlPatterns("/boards/*"); // /boards/ 로 시작하는 모든 요청에 대해 검사

        // bean.addUrlPatterns("/replies/*"); // /replies/ 로 시작하는 모든 요청에 대해 검사
        bean.setOrder(2); // 필터 순서
        return bean;
    }
}
