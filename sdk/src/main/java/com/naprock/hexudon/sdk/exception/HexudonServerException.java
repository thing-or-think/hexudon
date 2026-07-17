package com.naprock.hexudon.sdk.exception;

/**
 * Exception thrown when Hexudon server returns a server-side error.
 *
 * <p>
 * Represents HTTP 5xx errors such as:
 * <ul>
 *     <li>500 Internal Server Error</li>
 *     <li>502 Bad Gateway</li>
 *     <li>503 Service Unavailable</li>
 *     <li>504 Gateway Timeout</li>
 * </ul>
 *
 * <p>
 * This exception contains the HTTP status code returned by server
 * to help applications decide retry or pause strategies.
 */
public class HexudonServerException extends HexudonException {

    private final int statusCode;

    /**
     * Creates a server exception with HTTP status code.
     *
     * @param message error message from server
     * @param statusCode HTTP status code
     */
    public HexudonServerException(
            String message,
            int statusCode
    ) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Returns HTTP status code returned by server.
     *
     * @return HTTP error status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}