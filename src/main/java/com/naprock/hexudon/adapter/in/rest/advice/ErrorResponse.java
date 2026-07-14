package com.naprock.hexudon.adapter.in.rest.advice;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String errorCode;
    private String message;
    private long timestamp;
    private List<ValidationErrorDetail> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(
            String errorCode,
            String message,
            long timestamp
    ) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
    }

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