package com.study.shop.domain.member.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class MemberNotFoundException extends CustomException {
    public MemberNotFoundException(Long id) {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
  public MemberNotFoundException(String email) {
    super(ErrorCode.MEMBER_NOT_FOUND);
  }
}
