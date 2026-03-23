package com.example.travel_platform.user;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.travel_platform._core.handler.ex.Exception500;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSessionRegistry {

    private static final String USER_SESSION_KEY_PREFIX = "session:user:";

    private final StringRedisTemplate redisTemplate;

    @Value("${server.servlet.session.timeout:30m}")
    private Duration sessionTimeout;

    public void register(Integer userId, String sessionId) {
        if (userId == null || isBlank(sessionId)) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(createKey(userId), sessionId, sessionTimeout);
        } catch (DataAccessException e) {
            throw new Exception500("세션 저장소 등록 중 오류가 발생했습니다.");
        }
    }

    public boolean isCurrentSession(Integer userId, String sessionId) {
        if (userId == null || isBlank(sessionId)) {
            return true;
        }

        try {
            String currentSessionId = redisTemplate.opsForValue().get(createKey(userId));
            if (isBlank(currentSessionId)) {
                return true;
            }
            return sessionId.equals(currentSessionId);
        } catch (DataAccessException e) {
            throw new Exception500("세션 저장소 조회 중 오류가 발생했습니다.");
        }
    }

    public void remove(Integer userId, String sessionId) {
        if (userId == null) {
            return;
        }

        try {
            String key = createKey(userId);
            String currentSessionId = redisTemplate.opsForValue().get(key);

            if (isBlank(currentSessionId) || isBlank(sessionId) || sessionId.equals(currentSessionId)) {
                redisTemplate.delete(key);
            }
        } catch (DataAccessException e) {
            throw new Exception500("세션 저장소 삭제 중 오류가 발생했습니다.");
        }
    }

    private String createKey(Integer userId) {
        return USER_SESSION_KEY_PREFIX + userId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
