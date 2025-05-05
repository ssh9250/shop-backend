package com.study.cruisin.domain.board.exception;

import com.study.cruisin.global.exception.CustomException;
import com.study.cruisin.global.exception.ErrorCode;

public class PostNotFoundException extends CustomException {
    public PostNotFoundException(Long id) {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
