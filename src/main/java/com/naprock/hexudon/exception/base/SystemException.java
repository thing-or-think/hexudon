package com.naprock.hexudon.exception.base;

import com.naprock.hexudon.exception.code.ErrorCode;

/**
 * Base class for all system exceptions.
 *
 * <p>Represents technical failures that are typically caused by
 * infrastructure, configuration, I/O, serialization, or unexpected
 * runtime errors.
 *
 * <p>This class is independent of Spring Framework.
 */
public class SystemException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Creates a system exception.
     *
     * @param errorCode system error code
     * @param message error message
     */
    public SystemException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode != null
                ? errorCode
                : ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Creates a system exception with root cause.
     *
     * @param errorCode system error code
     * @param message error message
     * @param cause root cause
     */
    public SystemException(
            ErrorCode errorCode,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode != null
                ? errorCode
                : ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Returns the system error code.
     *
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}