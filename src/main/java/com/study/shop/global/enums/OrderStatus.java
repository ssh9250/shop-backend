package com.study.shop.global.enums;

import com.study.shop.domain.order.exception.InvalidOrderStatusException;

public enum OrderStatus {
    ORDERED, CANCELLED, PENDING, IN_DELIVERY, COMPLETED;

    public OrderStatus next() {
        return switch (this) {
            case PENDING -> ORDERED;
            case ORDERED -> IN_DELIVERY;
            case IN_DELIVERY -> COMPLETED;
            default -> throw InvalidOrderStatusException.InvalidState(this);
        };
    }

    public boolean canCancel() {
        return this == PENDING;
    }
}
