package com.study.shop.security.refresh;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RefreshTokenRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "refresh:";

    public RefreshTokenRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void store(String email, String refreshToken, long ttl) {
        String key = KEY_PREFIX + email;
        stringRedisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(ttl));
    }

    public String findByEmail(String email) {
        String key = KEY_PREFIX + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String email) {
        String key = KEY_PREFIX + email;
        stringRedisTemplate.delete(key);
    }

    public boolean exists(String email) {
        String key = KEY_PREFIX + email;
        return stringRedisTemplate.hasKey(key);
    }
}
