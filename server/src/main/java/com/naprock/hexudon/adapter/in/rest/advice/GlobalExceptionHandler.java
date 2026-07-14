package com.naprock.hexudon.adapter.in.rest.advice;


import com.naprock.hexudon.domain.exception.base.BusinessException;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
    }

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
