package com.naprock.hexudon.sdk.exception;

/**
 * Exception thrown when the Hexudon server encounters an internal error.
 * <p>
 * This exception is typically raised when the server responds with an
 * HTTP 5xx status code after all retry attempts have been exhausted.
 * <p>
 * The HTTP status code is retained to help client applications determine
 * an appropriate recovery strategy.
 */
public class HexudonServerException extends HexudonException {

    /**
     * HTTP status code returned by the server.
     */
    private final int statusCode;

    /**
     * Creates a new server exception.
     *
     * @param message    the detail message
     * @param statusCode the HTTP status code
     */
    public HexudonServerException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code returned by the server.
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}