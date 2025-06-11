package com.study.cruisin.domain.comment.exception;

import com.study.cruisin.global.exception.CustomException;
import com.study.cruisin.global.exception.ErrorCode;

public class CommentNotFoundException extends CustomException {
    public CommentNotFoundException(Long id) {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
