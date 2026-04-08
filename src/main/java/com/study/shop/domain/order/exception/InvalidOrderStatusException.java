package com.study.shop.domain.order.exception;

import com.study.shop.global.enums.OrderStatus;
import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class InvalidOrderStatusException extends CustomException {
    public InvalidOrderStatusException(OrderStatus next, OrderStatus status) {
        super(ErrorCode.INVALID_ORDER_STATE, next, status);
    }
    public InvalidOrderStatusException() {
        super(ErrorCode.INVALID_ORDER_CANCEL);
    }
}
