package com.naprock.hexudon.sdk.exception;

/**
 * Exception thrown when network communication fails.
 *
 * <p>
 * Represents transport-level failures such as:
 * <ul>
 *     <li>TCP connection errors.</li>
 *     <li>Connection timeout.</li>
 *     <li>Read/write I/O failures.</li>
 * </ul>
 *
 * <p>
 * This exception wraps the original network exception
 * to preserve the root cause and stack trace.
 */
public class HexudonNetworkException extends HexudonException {

    /**
     * Creates a network exception with message and root cause.
     *
     * @param message error description
     * @param cause original network exception
     */
    public HexudonNetworkException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}