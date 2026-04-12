package com.study.shop.domain.event;

import com.study.shop.infrastructure.redis.ViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostViewedEventListener {
    private final ViewCountService viewCountService;

    @Async
    @EventListener
    public void onPostViewed(PostViewedEvent event) {
        viewCountService.increment(event.getPostId());
    }
}
