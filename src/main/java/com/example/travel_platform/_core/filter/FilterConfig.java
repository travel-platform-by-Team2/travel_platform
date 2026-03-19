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
                bean.addUrlPatterns(
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

                bean.setOrder(2);
                return bean;
        }

        @Bean
        public FilterRegistrationBean<AdminFilter> adminFilter() {
                FilterRegistrationBean<AdminFilter> bean = new FilterRegistrationBean<>();
                bean.setFilter(new AdminFilter());
                bean.addUrlPatterns("/admin", "/admin/*");
                bean.setOrder(1);
                return bean;
        }
}
