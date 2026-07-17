package com.naprock.hexudon.sdk.exception;

import java.util.List;
import java.util.Objects;

/**
 * Exception thrown when request validation fails.
 *
 * <p>
 * Represents validation errors returned by Hexudon Server
 * when client sends invalid request data.
 *
 * <p>
 * This exception is thrown when server responds with:
 * <ul>
 *     <li>HTTP 400 Bad Request</li>
 *     <li>HTTP 422 Unprocessable Entity</li>
 * </ul>
 *
 * <p>
 * The exception contains detailed validation errors through
 * {@link ErrorResponse}.
 */
public class HexudonValidationException extends HexudonException {

    private final ErrorResponse errorResponse;

    /**
     * Creates a validation exception.
     *
     * @param message exception message
     * @param errorResponse detailed validation error response
     */
    public HexudonValidationException(
            String message,
            ErrorResponse errorResponse
    ) {
        super(message);

        this.errorResponse = Objects.requireNonNull(
                errorResponse,
                "errorResponse must not be null"
        );
    }

    /**
     * Returns detailed validation error response.
     *
     * @return validation error response
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }


    /**
     * Wrapper object containing validation error details.
     *
     * @param detail list of validation errors
     */
    public static record ErrorResponse(
            List<ValidationErrorDetail> detail
    ) {

        public ErrorResponse {

            Objects.requireNonNull(
                    detail,
                    "detail must not be null"
            );

            detail = List.copyOf(detail);
        }
    }


    /**
     * Represents a single validation error returned by server.
     *
     * @param loc location path of invalid field
     * @param msg validation error message
     * @param type validation error type
     */
    public static record ValidationErrorDetail(
            List<String> loc,
            String msg,
            String type
    ) {

        public ValidationErrorDetail {

            Objects.requireNonNull(
                    loc,
                    "loc must not be null"
            );

            Objects.requireNonNull(
                    msg,
                    "msg must not be null"
            );

            Objects.requireNonNull(
                    type,
                    "type must not be null"
            );

            loc = List.copyOf(loc);
        }
    }
}