package com.naprock.hexudon.domain.exception.business;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>Examples:
 * <ul>
 *     <li>Team does not exist</li>
 *     <li>Agent does not exist</li>
 *     <li>Cell does not exist</li>
 * </ul>
 *
 * <p>This exception represents a business-level not found error
 * and uses HTTP status 404.
 */
public class ResourceNotFoundException extends BusinessException {

    private static final int NOT_FOUND_STATUS = 404;

    /**
     * Creates a resource not found exception.
     *
     * @param errorCode specific resource error code
     * @param message error message
     */
    public ResourceNotFoundException(
            ErrorCode errorCode,
            String message
    ) {
        super(
                errorCode,
                NOT_FOUND_STATUS,
                message
        );
    }

    /**
     * Creates a resource not found exception with root cause.
     *
     * @param errorCode specific resource error code
     * @param message error message
     * @param cause root cause
     */
    public ResourceNotFoundException(
            ErrorCode errorCode,
            String message,
            Throwable cause
    ) {
        super(
                errorCode,
                NOT_FOUND_STATUS,
                message,
                cause
        );
    }
}