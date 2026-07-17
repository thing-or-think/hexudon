package com.naprock.hexudon.sdk.model.response;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO containing detailed information about a validation error.
 *
 * @param loc the location path identifying the invalid field
 * @param msg the validation error message
 * @param type the validation error type
 */
public record ValidationErrorDetail(
        List<String> loc,
        String msg,
        String type
) {

    /**
     * Creates a new {@code ValidationErrorDetail}.
     * <p>
     * If {@code loc} is {@code null}, an empty immutable list is used.
     *
     * @throws NullPointerException if {@code msg} or {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code msg} or {@code type} is blank
     */
    public ValidationErrorDetail {
        loc = loc == null
                ? List.of()
                : List.copyOf(loc);

        Objects.requireNonNull(msg, "msg must not be null");
        Objects.requireNonNull(type, "type must not be null");

        if (msg.isBlank()) {
            throw new IllegalArgumentException("msg must not be blank");
        }

        if (type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
    }
}