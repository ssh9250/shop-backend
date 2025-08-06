package com.study.shop.domain.instrument.exception;

import com.study.shop.global.exception.CustomException;
import com.study.shop.global.exception.ErrorCode;

public class InstrumentNotFoundException extends CustomException {
    public InstrumentNotFoundException(Long id) {
        super(ErrorCode.INSTRUMENT_NOT_FOUND);
    }
}
