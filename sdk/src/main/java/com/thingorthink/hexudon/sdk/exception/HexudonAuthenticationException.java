package com.thingorthink.hexudon.sdk.exception;

/**
 * Exception thrown when authentication or authorization fails.
 * <p>
 * This exception is typically raised when the server responds with:
 * <ul>
 *     <li>HTTP 401 Unauthorized</li>
 *     <li>HTTP 403 Forbidden</li>
 * </ul>
 * <p>
 * Client applications may catch this exception to stop execution,
 * prompt for a new authentication token, or notify the user.
 */
public class HexudonAuthenticationException extends HexudonException {

    /**
     * Creates a new authentication exception.
     *
     * @param message the detail message
     */
    public HexudonAuthenticationException(String message) {
        super(message);
    }
}
