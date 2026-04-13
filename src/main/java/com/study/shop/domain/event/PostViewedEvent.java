package com.study.shop.domain.event;

public record PostViewedEvent(Long postId, String ip) {
}
