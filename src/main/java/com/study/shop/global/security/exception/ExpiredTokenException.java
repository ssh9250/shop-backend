package com.study.shop.global.security.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class ExpiredTokenException extends CustomException {
    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}
