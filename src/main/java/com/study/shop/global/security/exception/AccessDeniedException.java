package com.study.shop.global.security.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

// 권한 없음 (403)
public class AccessDeniedException extends CustomException {
    public AccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
