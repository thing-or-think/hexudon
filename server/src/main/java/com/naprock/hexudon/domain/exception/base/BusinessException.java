package com.naprock.hexudon.domain.exception.base;

import com.naprock.hexudon.domain.exception.code.ErrorCode;

public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final int status;

    protected BusinessException(
            ErrorCode errorCode,
            int status
    ) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    protected BusinessException(
            ErrorCode errorCode,
            int status,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    protected BusinessException(
            ErrorCode errorCode,
            int status,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
}