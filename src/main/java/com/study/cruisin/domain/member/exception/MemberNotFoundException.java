package com.study.cruisin.domain.member.exception;

import com.study.cruisin.global.exception.CustomException;
import com.study.cruisin.global.exception.ErrorCode;

public class MemberNotFoundException extends CustomException {
    public MemberNotFoundException(Long id) {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
  public MemberNotFoundException(String email) {
    super(ErrorCode.MEMBER_NOT_FOUND);
  }
}
