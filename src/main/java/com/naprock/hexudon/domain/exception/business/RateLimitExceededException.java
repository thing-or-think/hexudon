package com.naprock.hexudon.domain.exception.business;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Exception thrown when a client exceeds the allowed request rate.
 *
 * <p>This exception is used to prevent abuse or spam requests
 * from clients.
 *
 * <p>Typically thrown by:
 * <ul>
 *     <li>RateLimiterInterceptor</li>
 *     <li>API request throttling mechanisms</li>
 * </ul>
 */
public class RateLimitExceededException extends BusinessException {

    private static final int TOO_MANY_REQUESTS_STATUS = 429;

    /**
     * Creates a rate limit exceeded exception.
     *
     * <p>Error code is fixed:
     * {@link ErrorCode#RATE_LIMIT_EXCEEDED}
     *
     * @param message error message
     */
    public RateLimitExceededException(String message) {
        super(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                TOO_MANY_REQUESTS_STATUS,
                message
        );
    }

    /**
     * Creates a rate limit exceeded exception with root cause.
     *
     * <p>Error code is fixed:
     * {@link ErrorCode#RATE_LIMIT_EXCEEDED}
     *
     * @param message error message
     * @param cause root cause
     */
    public RateLimitExceededException(
            String message,
            Throwable cause
    ) {
        super(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                TOO_MANY_REQUESTS_STATUS,
                message,
                cause
        );
    }
}