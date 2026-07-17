package com.naprock.hexudon.sdk.exception;

import com.naprock.hexudon.sdk.model.response.ErrorResponse;

import java.util.Objects;

/**
 * Exception thrown when a request fails server-side validation.
 *
 * <p>
 * This exception represents HTTP 422 Unprocessable Entity responses.
 * It contains detailed validation errors returned by the server,
 * allowing bot applications to analyze invalid parameters and
 * adjust their actions.
 */
public final class HexudonValidationException extends HexudonException {

    private final ErrorResponse errorResponse;


    /**
     * Creates a validation exception with detailed server errors.
     *
     * @param message error message
     * @param errorResponse server validation error details
     */
    public HexudonValidationException(
            String message,
            ErrorResponse errorResponse
    ) {

        super(message);

        this.errorResponse =
                Objects.requireNonNull(
                        errorResponse,
                        "errorResponse must not be null"
                );
    }


    /**
     * Returns validation error details from server.
     *
     * @return error response
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}