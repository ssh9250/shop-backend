package com.study.shop.domain.Item.exception;

import com.study.shop.global.enums.ItemStatus;
import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class InvalidAccessException extends CustomException {
    private InvalidAccessException(ItemStatus itemStatus) {
        super(ErrorCode.INVALID_ITEM_ACCESS, itemStatus);
    }

    public static InvalidAccessException notOrderable(ItemStatus itemStatus) {
        return new InvalidAccessException(itemStatus);
    }

}
