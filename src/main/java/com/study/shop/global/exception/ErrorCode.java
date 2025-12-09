package com.study.shop.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Controller 레벨
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSTRUMENT_NOT_FOUND("악기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

    // Security 관련
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // Refresh Token 관련
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISMATCH("리프레시 토큰이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
    ;

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
