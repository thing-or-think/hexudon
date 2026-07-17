package com.naprock.hexudon.sdk.model.response;

import java.util.List;

/**
 * Response DTO containing validation error details returned by the server.
 *
 * @param detail the list of validation errors
 */
public record ErrorResponse(
        List<ValidationErrorDetail> detail
) {

    /**
     * Creates a new {@code ErrorResponse}.
     * <p>
     * If {@code detail} is {@code null}, an empty immutable list is used.
     */
    public ErrorResponse {
        detail = detail == null
                ? List.of()
                : List.copyOf(detail);
    }
}