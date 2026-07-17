package com.naprock.hexudon.sdk.exception;

/**
 * Exception thrown when authentication or authorization fails.
 *
 * <p>
 * Represents HTTP 401 Unauthorized and HTTP 403 Forbidden responses.
 * This exception indicates that the provided authentication token
 * or team identity is invalid.
 *
 * <p>
 * Bot applications should catch this exception and verify their
 * authentication configuration before continuing execution.
 */
public final class HexudonAuthenticationException
        extends HexudonException {


    /**
     * Creates an authentication exception.
     *
     * @param message error message
     */
    public HexudonAuthenticationException(
            String message
    ) {
        super(message);
    }
}