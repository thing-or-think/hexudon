package com.naprock.hexudon.exception.response;

/**
 * DTO representing detailed validation errors for a specific field.
 *
 * <p>Used when DTO validation fails to provide information about:
 * <ul>
 *     <li>The invalid field</li>
 *     <li>The rejected value</li>
 *     <li>The validation failure message</li>
 * </ul>
 *
 * <p>This record is included in {@code ErrorResponse}.
 */
public record ValidationErrorDetail(

        String field,
        String rejectedValue,
        String message

) {
}