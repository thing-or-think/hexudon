package com.naprock.hexudon.sdk.exception;

import java.util.List;
import java.util.Objects;

/**
 * Exception thrown when the server rejects a request because
 * of validation errors (typically HTTP 400 or 422).
 */
public class HexudonValidationException extends HexudonException {

    private final ErrorResponse errorResponse;

    /**
     * Creates a new validation exception.
     *
     * @param message       the detail message
     * @param errorResponse validation error details
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
     * Creates a new validation exception with a default empty error response.
     *
     * @param message the detail message
     */
    public HexudonValidationException(String message) {
        super(message);
        this.errorResponse = new ErrorResponse(java.util.List.of());
    }

    /**
     * Returns the validation error details.
     *
     * @return validation error response
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    /**
     * Wrapper for validation error details returned by the server.
     *
     * @param detail validation error list
     */
    public static record ErrorResponse(
            List<ValidationErrorDetail> detail
    ) {

        /**
         * Compact constructor.
         */
        public ErrorResponse {
            Objects.requireNonNull(detail, "detail must not be null");
            detail = List.copyOf(detail);
        }
    }

    /**
     * Represents a single validation error.
     *
     * @param loc  error location (e.g. ["body","actions",0])
     * @param msg  validation message
     * @param type validation error type
     */
    public static record ValidationErrorDetail(
            List<String> loc,
            String msg,
            String type
    ) {

        /**
         * Compact constructor.
         */
        public ValidationErrorDetail {
            Objects.requireNonNull(loc, "loc must not be null");
            Objects.requireNonNull(msg, "msg must not be null");
            Objects.requireNonNull(type, "type must not be null");

            loc = List.copyOf(loc);
        }
    }
}
