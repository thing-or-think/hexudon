package com.naprock.hexudon.domain.exception.handler;

import com.naprock.hexudon.domain.exception.base.BusinessException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.exception.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessException_shouldReturnConfiguredStatusAndDetails() {
        // BusinessException is abstract, we can instantiate a subclass
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
}
