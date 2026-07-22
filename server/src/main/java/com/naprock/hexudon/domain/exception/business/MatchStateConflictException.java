package com.naprock.hexudon.domain.exception.business;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Exception thrown when an operation is invalid for the current match state.
 *
 * <p>Examples:
 * <ul>
 *     <li>Submit action when match is not in RUNNING state</li>
 *     <li>Start match that is already started</li>
 *     <li>Modify match after it has finished</li>
 * </ul>
 *
 * <p>This exception represents a business rule violation
 * related to match lifecycle.
 */
public class MatchStateConflictException extends BusinessException {

    private static final int BAD_REQUEST_STATUS = 400;

    public MatchStateConflictException(ErrorCode errorCode) {
        super(
                errorCode,
                BAD_REQUEST_STATUS,
                errorCode.getDefaultMessage()
        );
    }

    /**
     * Creates a match state conflict exception.
     *
     * @param errorCode specific match state error code
     * @param message error message
     */
    public MatchStateConflictException(
            ErrorCode errorCode,
            String message
    ) {
        super(
                errorCode,
                BAD_REQUEST_STATUS,
                message
        );
    }

    /**
     * Creates a match state conflict exception with root cause.
     *
     * @param errorCode specific match state error code
     * @param message error message
     * @param cause root cause
     */
    public MatchStateConflictException(
            ErrorCode errorCode,
            String message,
            Throwable cause
    ) {
        super(
                errorCode,
                BAD_REQUEST_STATUS,
                message,
                cause
        );
    }
}