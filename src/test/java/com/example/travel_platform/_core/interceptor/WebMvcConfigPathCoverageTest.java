package com.example.travel_platform._core.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.ServletRequestPathUtils;

import com.example.travel_platform.user.UserSessionChecker;

class WebMvcConfigPathCoverageTest {

    private final UserSessionChecker userSessionChecker = new UserSessionChecker(null);
    private final LoginInterceptor loginInterceptor = new LoginInterceptor(userSessionChecker);
    private final AdminInterceptor adminInterceptor = new AdminInterceptor(userSessionChecker);
    private final WebMvcConfig webMvcConfig = new WebMvcConfig(loginInterceptor, adminInterceptor);

    @Test
    void mypageNested() {
        assertTrue(loginMapped().matches(request("GET", "/mypage/bookings/1")));
    }

    @Test
    void boardEdit() {
        assertTrue(loginMapped().matches(request("GET", "/boards/1/edit")));
    }

    @Test
    void boardApi() {
        assertTrue(loginMapped().matches(request("POST", "/api/boards/1/likes/toggle")));
    }

    @Test
    void tripApi() {
        assertTrue(loginMapped().matches(request("POST", "/api/trips/1/places")));
    }

    @Test
    void adminNested() {
        assertTrue(adminMapped().matches(request("POST", "/admin/boards/1/delete")));
    }

    @Test
    void bookingOpen() {
        assertFalse(loginMapped().matches(request("GET", "/bookings/checkout")));
    }

    @Test
    void boardDetailPass() throws Exception {
        boolean result = loginInterceptor.preHandle(
                request("GET", "/boards/1"),
                new MockHttpServletResponse(),
                new Object());

        assertTrue(result);
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        ServletRequestPathUtils.parseAndCache(request);
        return request;
    }

    private MappedInterceptor loginMapped() {
        return mappedInterceptors().stream()
                .filter(mapped -> mapped.getInterceptor() == loginInterceptor)
                .findFirst()
                .orElseThrow();
    }

    private MappedInterceptor adminMapped() {
        return mappedInterceptors().stream()
                .filter(mapped -> mapped.getInterceptor() == adminInterceptor)
                .findFirst()
                .orElseThrow();
    }

    private List<MappedInterceptor> mappedInterceptors() {
        TestInterceptorRegistry registry = new TestInterceptorRegistry();
        webMvcConfig.addInterceptors(registry);
        return registry.exposedInterceptors().stream()
                .filter(MappedInterceptor.class::isInstance)
                .map(MappedInterceptor.class::cast)
                .toList();
    }

    private static class TestInterceptorRegistry extends InterceptorRegistry {
        public List<Object> exposedInterceptors() {
            return super.getInterceptors();
        }
    }
}
