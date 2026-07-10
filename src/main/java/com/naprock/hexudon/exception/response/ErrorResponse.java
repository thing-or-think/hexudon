package com.naprock.hexudon.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard error response DTO returned by REST API.
 *
 * <p>This class provides a unified structure for all API errors:
 * <ul>
 *     <li>Error code identifier</li>
 *     <li>Error message</li>
 *     <li>Error timestamp</li>
 *     <li>Validation details (optional)</li>
 * </ul>
 *
 * <p>Created by GlobalExceptionHandler.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Error code identifier.
     */
    private String errorCode;

    /**
     * Detailed error message.
     */
    private String message;

    /**
     * Error occurrence timestamp in epoch milliseconds.
     */
    private long timestamp;

    /**
     * Validation error details.
     */
    private List<ValidationErrorDetail> errors;

    /**
     * Default constructor required by Jackson.
     */
    public ErrorResponse() {
    }

    /**
     * Creates a standard error response.
     *
     * @param errorCode error code
     * @param message error message
     * @param timestamp error timestamp
     */
    public ErrorResponse(
            String errorCode,
            String message,
            long timestamp
    ) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Creates an error response with validation details.
     *
     * @param errorCode error code
     * @param message error message
     * @param timestamp error timestamp
     * @param errors validation error list
     */
    public ErrorResponse(
            String errorCode,
            String message,
            long timestamp,
            List<ValidationErrorDetail> errors
    ) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ValidationErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationErrorDetail> errors) {
        this.errors = errors;
    }
}