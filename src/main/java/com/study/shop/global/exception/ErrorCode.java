package com.study.shop.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // MEMBER
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),

    // POST
    POST_NOT_FOUND("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // COMMENT
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ITEM
    ITEM_NOT_FOUND("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STOCK_NOT_ENOUGH("재고가 부족합니다.", HttpStatus.CONFLICT),
    INVALID_ITEM_ACCESS("주문할 수 없는 상품입니다. 아이템 상태 : %s", HttpStatus.BAD_REQUEST),

    // ORDER
    ORDER_NOT_FOUND("주문을 찾을 수 없습니다.",  HttpStatus.NOT_FOUND),
    INVALID_ORDER_TRANSITION("주문 상태를 %s(으)로 변경할 수 없습니다. 현재 상태: %s", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATE("현재 상태 %s 에서는 주문 상태를 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_CANCEL("주문 수락 전에만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST),

    // etc
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

    // Security 관련
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // Refresh Token 관련
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISMATCH("리프레시 토큰이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
