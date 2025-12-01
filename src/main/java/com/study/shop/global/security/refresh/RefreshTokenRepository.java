package com.study.shop.global.security.refresh;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String TOKEN_KEY = "refresh:";

    public RefreshTokenRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void store(String email, String refreshToken, long ttl) {
        String key = TOKEN_KEY + email;
        stringRedisTemplate.opsForValue().set(key, refreshToken, ttl);
    }

    public String  findByEmail(String email) {
        String key = TOKEN_KEY + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String email) {
        String key = TOKEN_KEY + email;
        stringRedisTemplate.delete(key);
    }
    public boolean exists(String email) {
        String key = TOKEN_KEY + email;
        return stringRedisTemplate.hasKey(key);
    }

    // 존재하지 않으면 -2, 만료 시간 없으면 -1
    public long getRemainingTtl(String email) {
        String key = TOKEN_KEY + email;
        return stringRedisTemplate.getExpire(key) * 1000;
    }
}
