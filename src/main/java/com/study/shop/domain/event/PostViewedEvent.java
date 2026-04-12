package com.study.shop.domain.event;

public class PostViewedEvent {
    private final Long postId;
    public PostViewedEvent(Long postId) {
        this.postId = postId;
    }
    public Long getPostId() {
        return postId;
    }
}
