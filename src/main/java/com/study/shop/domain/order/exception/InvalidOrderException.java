package com.study.shop.domain.order.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class InvalidOrderException extends CustomException {
    private InvalidOrderException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static InvalidOrderException selfPurchaseException(){
        return new InvalidOrderException(ErrorCode.SELF_PURCHASE);
    }
}
