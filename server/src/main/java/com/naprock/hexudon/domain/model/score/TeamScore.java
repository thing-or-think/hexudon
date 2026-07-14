package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.map.UdonType;

import java.util.*;

/**
 * Entity responsible for tracking a team's score and performance statistics.
 */
public class TeamScore {

    private final String teamName;

    private final Set<UdonType> collectedUdonTypes;
    private final Map<Integer, Set<UdonType>> dailyUdonTypesHistory;

    private int totalServings;
    private long totalResponseTimeMs;
    private int requestCount;


    public TeamScore(String teamName) {
        validateTeamId(teamName);

        this.teamName = teamName;
        this.collectedUdonTypes = new HashSet<>();
        this.dailyUdonTypesHistory = new HashMap<>();

        this.totalServings = 0;
        this.totalResponseTimeMs = 0;
        this.requestCount = 0;
    }

    public String getTeamName() {
        return teamName;
    }

    public Set<UdonType> getCollectedUdonTypes() {
        return Collections.unmodifiableSet(collectedUdonTypes);
    }

    public Map<Integer, Set<UdonType>> getDailyUdonTypesHistory() {
        return Collections.unmodifiableMap(dailyUdonTypesHistory);
    }

    public int getTotalServings() {
        return totalServings;
    }

    public long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public int getRequestCount() {
        return requestCount;
    }


    public int getUniqueUdonTypesCount() {
        return collectedUdonTypes.size();
    }


    public int getAccumulatedDailyUdonTypes() {
        return dailyUdonTypesHistory.values()
                .stream()
                .mapToInt(Set::size)
                .sum();
    }


    public void incrementServings() {
        totalServings++;
    }


    public void addUdonCollection(int turn, UdonType udonType) {
        validateTurn(turn);

        Objects.requireNonNull(udonType, "udonType");

        collectedUdonTypes.add(udonType);

        dailyUdonTypesHistory
                .computeIfAbsent(turn, key -> new HashSet<>())
                .add(udonType);
    }


    public void addResponseTime(long durationMs) {
        if (durationMs < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "durationMs must not be negative"
            );
        }

        totalResponseTimeMs += durationMs;
        requestCount++;
    }


    private void validateTeamId(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamName must not be null"
            );
        }
    }


    private void validateTurn(int turn) {
        if (turn <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "turn not be negative"
            );
        }
    }
}