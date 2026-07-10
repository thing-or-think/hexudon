package com.naprock.hexudon.domain.exception.business;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Exception thrown when an action violates game rules.
 *
 * <p>Examples:
 * <ul>
 *     <li>Agent moves to an invalid cell</li>
 *     <li>Team submits an invalid action plan</li>
 *     <li>Action exceeds game constraints</li>
 *     <li>Illegal resource collection</li>
 * </ul>
 *
 * <p>This exception represents a business rule violation
 * detected by game engine components.
 */
public class GameRuleViolationException extends BusinessException {

    private static final int BAD_REQUEST_STATUS = 400;

    /**
     * Creates a game rule violation exception.
     *
     * @param errorCode specific game rule error code
     * @param message error message
     */
    public GameRuleViolationException(
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
     * Creates a game rule violation exception with root cause.
     *
     * @param errorCode specific game rule error code
     * @param message error message
     * @param cause root cause
     */
    public GameRuleViolationException(
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