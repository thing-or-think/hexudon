package com.naprock.hexudon.infrastructure.interceptor;

import com.naprock.hexudon.application.port.in.IncreaseSpamViolationUseCase;
import com.naprock.hexudon.domain.exception.business.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimiterInterceptorTest {

    private IncreaseSpamViolationUseCase increaseSpamViolationUseCase;
    private RateLimiterInterceptor interceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        increaseSpamViolationUseCase = mock(IncreaseSpamViolationUseCase.class);
        interceptor = new RateLimiterInterceptor(increaseSpamViolationUseCase);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @Test
    void preHandle_shouldAllowNonActionsEndpoint() {
        when(request.getRequestURI()).thenReturn("/api/match/state");

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        verifyNoInterceptors();
    }

    @Test
    void preHandle_shouldAllowActionsEndpointWhenTeamHeaderMissing() {
        when(request.getRequestURI()).thenReturn("/api/match/actions");
        when(request.getHeader("X-Team-Name")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        verifyNoInterceptors();
    }

    @Test
    void preHandle_shouldTriggerRateLimitAndSpamUsecase() {
        when(request.getRequestURI()).thenReturn("/api/match/actions");
        when(request.getHeader("X-Team-Name")).thenReturn("Alpha");

        // First 5 requests should pass
        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(request, response, handler));
        }

        // 6th request should fail and throw RateLimitExceededException
        assertThrows(RateLimitExceededException.class,
                () -> interceptor.preHandle(request, response, handler));

        // Usecase should be called to record spam violation
        verify(increaseSpamViolationUseCase, times(1)).increaseSpamViolationCount("Alpha");
    }

    private void verifyNoInterceptors() {
        verifyNoInteractions(increaseSpamViolationUseCase);
    }
}
