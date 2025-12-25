package com.study.shop.test;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class RedisTestController {
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis")
    public String testRedis() {
        //given
        stringRedisTemplate.opsForValue().set("testKey", "Hello, Redis!!");

        //when
        String value = stringRedisTemplate.opsForValue().get("testKey");

        //then
        return "Redis Value: " + value;
    }

}
