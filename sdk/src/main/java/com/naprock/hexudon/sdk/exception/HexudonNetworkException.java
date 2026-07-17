package com.naprock.hexudon.sdk.exception;

/**
 * Exception thrown when a network communication error occurs.
 * <p>
 * This exception is typically raised when the SDK encounters low-level
 * networking failures such as:
 * <ul>
 *     <li>Connection timeout</li>
 *     <li>Socket errors</li>
 *     <li>DNS resolution failures</li>
 *     <li>I/O errors after the final retry attempt</li>
 *     <li>InterruptedException during request execution</li>
 * </ul>
 * <p>
 * The original cause is preserved to help diagnose networking issues.
 */
public class HexudonNetworkException extends HexudonException {

    /**
     * Creates a new network exception with a detail message.
     *
     * @param message the detail message
     */
    public HexudonNetworkException(String message) {
        super(message);
    }

    /**
     * Creates a new network exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying network exception
     */
    public HexudonNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}