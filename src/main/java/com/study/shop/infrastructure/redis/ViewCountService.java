package com.study.shop.infrastructure.redis;

import com.study.shop.domain.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountService {
    private static final String VIEW_KEY_PREFIX = "view:post:";
    private static final String VIEW_KEY_PATTERN = "view:post:*";

    private final StringRedisTemplate stringRedisTemplate;
    private final PostRepository postRepository;

    public void increment(Long postId) {
        stringRedisTemplate.opsForValue()
                .increment(VIEW_KEY_PREFIX + postId);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void flushViewCountsToDb() {
        Set<String> keys = stringRedisTemplate.keys(VIEW_KEY_PATTERN);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null) {
                continue;
            }

            Long postId = Long.parseLong(key.replace(VIEW_KEY_PREFIX, ""));
            int count = Integer.parseInt(value);

            postRepository.findById(postId).ifPresent(post -> {
                post.updateViewCount(post.getViewCount() + count);
            });

            stringRedisTemplate.delete(key);
        }

        log.debug("ViewCount flush complete - {} posts updated", keys.size());
    }
}
