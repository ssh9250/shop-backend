package com.study.shop.domain.board.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class PostNotFoundException extends CustomException {
    public PostNotFoundException(Long id) {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
