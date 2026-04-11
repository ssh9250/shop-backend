package com.study.shop.domain.order.exception;

import com.study.shop.global.enums.OrderStatus;
import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class InvalidOrderStatusException extends CustomException {
    private InvalidOrderStatusException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static InvalidOrderStatusException InvalidTransition(OrderStatus next, OrderStatus status) {
        return new InvalidOrderStatusException(ErrorCode.INVALID_ORDER_TRANSITION, next, status);
    }

    public static InvalidOrderStatusException InvalidState(OrderStatus now) {
        return new InvalidOrderStatusException(ErrorCode.INVALID_ORDER_STATE, now);
    }
    public static InvalidOrderStatusException cannotCancel() {
        return new InvalidOrderStatusException(ErrorCode.INVALID_ORDER_CANCEL);
    }
}
