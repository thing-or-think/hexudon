package com.naprock.hexudon.domain.exception.handler;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.response.ErrorResponse;
import com.naprock.hexudon.domain.exception.response.ValidationErrorDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


/**
 * Global exception handler for all REST controllers.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Catch application exceptions globally</li>
 *     <li>Convert exceptions into standardized ErrorResponse</li>
 *     <li>Return appropriate HTTP status codes</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * Default constructor.
     */
    public GlobalExceptionHandler() {
    }


    /**
     * Handles business exceptions.
     *
     * @param ex business exception
     * @return standardized error response
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex
    ) {

        String code = ex.getErrorCode().name();
        String message = ex.getMessage();

        log.warn(
                "[Business Error] Code: {}, Message: {}",
                code,
                message
        );

        ErrorResponse response =
                new ErrorResponse(
                        code,
                        message,
                        System.currentTimeMillis()
                );

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }


    /**
     * Handles DTO validation errors.
     *
     * @param ex validation exception
     * @return validation error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        List<ValidationErrorDetail> errors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::mapValidationError)
                        .toList();


        log.info(
                "Validation failed for fields: {}",
                errors.stream()
                        .map(ValidationErrorDetail::field)
                        .toList()
        );


        ErrorResponse response =
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Request body validation failed.",
                        System.currentTimeMillis(),
                        errors
                );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /**
     * Handles malformed JSON request body.
     *
     * @param ex JSON parsing exception
     * @return invalid JSON response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(
            HttpMessageNotReadableException ex
    ) {

        log.warn(
                "Invalid JSON request body: {}",
                ex.getMessage()
        );


        ErrorResponse response =
                new ErrorResponse(
                        "INVALID_JSON_PAYLOAD",
                        "Malformed or invalid JSON request body.",
                        System.currentTimeMillis()
                );


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /**
     * Handles unexpected system exceptions.
     *
     * <p>Internal exception details are hidden from clients.
     *
     * @param ex unexpected exception
     * @return generic internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex
    ) {

        log.error(
                "Unexpected system error occurred.",
                ex
        );


        ErrorResponse response =
                new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred. Please contact the administrator.",
                        System.currentTimeMillis()
                );


        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    /**
     * Converts Spring FieldError into ValidationErrorDetail.
     *
     * @param error field error
     * @return validation detail
     */
    private ValidationErrorDetail mapValidationError(
            FieldError error
    ) {

        return new ValidationErrorDetail(
                error.getField(),
                error.getRejectedValue() != null
                        ? error.getRejectedValue().toString()
                        : null,
                error.getDefaultMessage()
        );
    }
}