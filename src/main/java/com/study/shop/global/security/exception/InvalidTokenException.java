package com.study.shop.global.security.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

// 토큰 관련
public class InvalidTokenException extends CustomException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
