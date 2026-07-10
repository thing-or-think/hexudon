package com.naprock.hexudon.infrastructure.interceptor;

import com.naprock.hexudon.application.port.in.IncreaseSpamViolationUseCase;
import com.naprock.hexudon.domain.exception.business.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {
    private static final String ACTION_API = "/api/match/actions";
    private static final String TEAM_HEADER = "X-Team-Name";

    private static final long REQUEST_WINDOW_MS = 1000L;
    private static final int MAX_REQUESTS_PER_SECOND = 5;

    private final Map<String, List<Long>> requestLog;
    private final IncreaseSpamViolationUseCase increaseSpamViolationCount;

    public RateLimiterInterceptor(IncreaseSpamViolationUseCase increaseSpamViolationCount) {
        this.requestLog = new ConcurrentHashMap<>();
        this.increaseSpamViolationCount = increaseSpamViolationCount;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!ACTION_API.equals(request.getRequestURI())) {
            return true;
        }
        String teamName = request.getHeader(TEAM_HEADER);

        if (teamName == null || teamName.isBlank()) {
            return true;
        }

        long now = System.currentTimeMillis();

        List<Long> timestamps = requestLog.computeIfAbsent(
                teamName,
                key -> Collections.synchronizedList(new ArrayList<>())
        );

        synchronized (timestamps) {

            // Remove expired timestamps
            timestamps.removeIf(time -> now - time >= REQUEST_WINDOW_MS);

            if (timestamps.size() >= MAX_REQUESTS_PER_SECOND) {

                // Increase spam violation count
                increaseSpamViolationCount.increaseSpamViolationCount(teamName);
                throw new RateLimitExceededException(
                        "Too many requests."
                );
            }

            timestamps.add(now);
        }

        return true;
    }

}
