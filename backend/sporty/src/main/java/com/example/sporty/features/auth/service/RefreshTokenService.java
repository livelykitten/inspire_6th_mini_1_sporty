package com.example.sporty.features.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String refreshToken, long expirationSeconds) {
        // 회원당 하나의 RT를 유지하며 재로그인 시 기존 값을 교체
        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                refreshToken,
                Duration.ofSeconds(expirationSeconds));
    }

    public void delete(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }
}
