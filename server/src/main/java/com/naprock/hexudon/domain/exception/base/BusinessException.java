package com.naprock.hexudon.domain.exception.base;

import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Base class for all business exceptions.
 *
 * <p>This exception encapsulates:
 * <ul>
 *     <li>ErrorCode - application-specific error code</li>
 *     <li>HTTP status - numeric HTTP status code</li>
 *     <li>Error message</li>
 * </ul>
 *
 * <p>This class is intentionally independent of Spring Framework.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final int status;

    /**
     * Creates a business exception.
     *
     * @param errorCode application error code
     * @param status HTTP status code
     * @param message error message
     */
    public BusinessException(ErrorCode errorCode, int status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    /**
     * Creates a business exception with root cause.
     *
     * @param errorCode application error code
     * @param status HTTP status code
     * @param message error message
     * @param cause root cause
     */
    public BusinessException(
            ErrorCode errorCode,
            int status,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    /**
     * Returns application error code.
     *
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns HTTP status code.
     *
     * @return HTTP status
     */
    public int getStatus() {
        return status;
    }
}