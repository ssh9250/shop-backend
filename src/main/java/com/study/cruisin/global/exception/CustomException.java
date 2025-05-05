package com.study.cruisin.global.exception;

public abstract class CustomException extends RuntimeException{
    private final ErrorCode errorCode;

    protected CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
