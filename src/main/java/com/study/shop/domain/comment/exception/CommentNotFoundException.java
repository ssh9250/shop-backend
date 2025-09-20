package com.study.shop.domain.comment.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class CommentNotFoundException extends CustomException {
    public CommentNotFoundException(Long id) {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
