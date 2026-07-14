package com.naprock.hexudon.infrastructure.interceptor;

import com.naprock.hexudon.domain.exception.business.RateLimitExceededException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterInterceptorTest {

    @Test
    void testRateLimitExceededExceptionProperties() {
        RateLimitExceededException exception = new RateLimitExceededException("Too many requests");

        assertEquals(ErrorCode.RATE_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals(429, exception.getStatus());
        assertEquals("Too many requests", exception.getMessage());
    }

    @Test
    void testRateLimitExceededExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        RateLimitExceededException exception = new RateLimitExceededException("Too many requests", cause);

        assertEquals(ErrorCode.RATE_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals(429, exception.getStatus());
        assertEquals("Too many requests", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
