package com.study.shop.domain.order.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class StockNotEnoughException extends CustomException {
    public StockNotEnoughException(Long id) {
        super(ErrorCode.STOCK_NOT_ENOUGH);
    }
}
