package com.naprock.hexudon.adapter.in.rest.advice;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessException_shouldReturnConfiguredStatusAndDetails() {
        BusinessException ex = new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, 429, "Too many requests.") {};

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);

        assertAll(
                () -> assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("RATE_LIMIT_EXCEEDED", response.getBody().getErrorCode()),
                () -> assertEquals("Too many requests.", response.getBody().getMessage())
        );
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        Exception ex = new NullPointerException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode()),
                () -> assertEquals("An unexpected error occurred. Please contact the administrator.", response.getBody().getMessage())
        );
    }

    @Test
    void handleValidationException_shouldReturnValidationErrorResponse() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("target", "teamId", "Alpha", false, null, null, "must not be blank");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode()),
                () -> assertEquals("Request body validation failed.", response.getBody().getMessage()),
                () -> assertNotNull(response.getBody().getErrors()),
                () -> assertEquals(1, response.getBody().getErrors().size()),
                () -> assertEquals("teamId", response.getBody().getErrors().get(0).field()),
                () -> assertEquals("Alpha", response.getBody().getErrors().get(0).rejectedValue()),
                () -> assertEquals("must not be blank", response.getBody().getErrors().get(0).message())
        );
    }

    @Test
    void handleJsonParseError_shouldReturnInvalidJsonPayloadResponse() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON");

        ResponseEntity<ErrorResponse> response = handler.handleJsonParseError(ex);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals("INVALID_JSON_PAYLOAD", response.getBody().getErrorCode()),
                () -> assertEquals("Malformed or invalid JSON request body.", response.getBody().getMessage())
        );
    }
}
