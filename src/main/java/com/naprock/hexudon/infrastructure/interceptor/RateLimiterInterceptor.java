package com.naprock.hexudon.infrastructure.interceptor;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.RateLimitExceededException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.manager.MatchManager;
import com.naprock.hexudon.domain.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.model.Team;
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

    private final MatchManager matchManager;
    private final Map<String, List<Long>> requestLog;

    public RateLimiterInterceptor(MatchManager matchManager) {
        if (matchManager == null) {
            throw new IllegalArgumentException(
                    "MatchManager must not be null"
            );
        }

        this.matchManager = matchManager;
        this.requestLog = new ConcurrentHashMap<>();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Không phải API action thì bỏ qua
        if (!ACTION_API.equals(uri)) {
            return true;
        }

        String teamName = request.getHeader(TEAM_HEADER);

        if (teamName == null || teamName.isBlank()) {
            throw new GameRuleViolationException(
                    ErrorCode.MISSING_REQUIRED_HEADER,
                    "Missing X-Team-Name header"
            );
        }

        MatchState matchState = matchManager.getMatchState();

        Team team = matchState.getTeams()
                .stream()
                .filter(t -> teamName.equals(t.getTeamName()))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.TEAM_NOT_FOUND,
                                "Team not found: " + teamName
                        )
                );

        if (team.isDisqualified()) {
            throw new GameRuleViolationException(
                    ErrorCode.TEAM_DISABLED,
                    "Team has been disqualified"
            );
        }

        MatchConfig config = matchManager.getMatchConfig();

        long now = System.currentTimeMillis();

        List<Long> timestamps = requestLog.computeIfAbsent(
                teamName,
                key -> Collections.synchronizedList(new ArrayList<>())
        );

        synchronized (timestamps) {

            cleanExpiredRequests(timestamps, now);

            if (timestamps.size() >= config.getMaxRequestsPerSecond()) {

                team.incrementSpamViolation();

                if (team.getSpamViolationCount()
                        >= config.getMaxSpamViolations()) {

                    team.setDisqualified(true);
                }

                throw new RateLimitExceededException(
                        "Too many requests."
                );
            }

            timestamps.add(now);
        }

        return true;
    }

    private void cleanExpiredRequests(
            List<Long> timestamps,
            long now
    ) {

        timestamps.removeIf(
                timestamp ->
                        now - timestamp >= REQUEST_WINDOW_MS
        );
    }
}
