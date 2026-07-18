package com.thingorthink.hexudon.sdk.exception;

/**
 * Exception thrown when JSON serialization or deserialization fails.
 */
public class HexudonSerializationException extends HexudonException {

    /**
     * Creates a new serialization exception.
     *
     * @param message the detail message
     */
    public HexudonSerializationException(String message) {
        super(message);
    }

    /**
     * Creates a new serialization exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public HexudonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
