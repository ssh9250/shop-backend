package com.study.shop.domain.Item.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class ItemNotFoundException extends CustomException {
    public ItemNotFoundException(Long id) {
        super(ErrorCode.ITEM_NOT_FOUND);
    }
}
