package com.study.shop.domain.member.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class DuplicateNicknameException extends CustomException {
    public DuplicateNicknameException(String nickname) {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }
}