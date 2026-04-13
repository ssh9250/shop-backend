package com.study.shop.domain.event;

import com.study.shop.infrastructure.redis.ViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostViewedEventListener {
    private final ViewCountService viewCountService;

    @Async
    @EventListener
    public void onPostViewed(PostViewedEvent event) {
        log.info("post viewed id: {}", event.postId());
        viewCountService.increment(event.postId(), event.ip());
    }
}
