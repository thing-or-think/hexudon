package com.naprock.hexudon.sdk.exception;

/**
 * Base runtime exception for Hexudon SDK.
 *
 * <p>
 * This exception represents an unrecoverable SDK error.
 * Specific SDK exceptions should extend this class.
 *
 * <p>
 * Applications using Hexudon SDK should catch this exception
 * to handle SDK failures, logging, and recovery decisions.
 */
public class HexudonException extends RuntimeException {

    /**
     * Creates an exception with an error message.
     *
     * @param message error description
     */
    public HexudonException(String message) {
        super(message);
    }

    /**
     * Creates an exception with an error message and root cause.
     *
     * @param message error description
     * @param cause original exception
     */
    public HexudonException(String message, Throwable cause) {
        super(message, cause);
    }
}