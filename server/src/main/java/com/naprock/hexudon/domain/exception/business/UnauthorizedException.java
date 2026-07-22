package com.naprock.hexudon.domain.exception.business;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

public class UnauthorizedException extends BusinessException {

    private static final int STATUS = 401;

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode, STATUS);
    }

    public UnauthorizedException(
            ErrorCode errorCode,
            String detail
    ) {
        super(errorCode, STATUS, detail);
    }

    public UnauthorizedException(
            ErrorCode errorCode,
            String detail,
            Throwable cause
    ) {
        super(errorCode, STATUS, detail, cause);
    }
}