package com.study.shop.domain.event;

public record OrderAcceptedEvent(Long orderId, Long itemId) {
}
