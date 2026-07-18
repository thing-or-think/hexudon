package com.naprock.hexudon.sdk.exception;

/**
 * Base unchecked exception for the Hexudon SDK.
 * <p>
 * All SDK-specific exceptions extend this class.
 * It is intended to be caught by client applications
 * for generic SDK error handling.
 */
public class HexudonException extends RuntimeException {

    /**
     * Creates a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public HexudonException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public HexudonException(String message, Throwable cause) {
        super(message, cause);
    }
}
