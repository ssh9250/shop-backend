package com.study.shop.domain.member.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
