package com.naprock.hexudon.domain.model.score;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;
import static com.naprock.hexudon.domain.validation.DomainValidator.requirePositive;

/**
 * Entity responsible for tracking a team's score and performance statistics.
 */
public class TeamScore {

    private final int teamId;

    /**
     * Unique Udon type IDs collected throughout the entire match.
     */
    private final Set<Integer> collectedUdonTypes;

    /**
     * Udon type IDs collected for each turn.
     * Key: turn
     * Value: unique Udon type IDs collected during that turn.
     */
    private final Map<Integer, Set<Integer>> dailyUdonTypesHistory;

    private int totalServings;
    private long totalResponseTimeMs;
    private int requestCount;

    public TeamScore(int teamId) {

        requireNonNegative(teamId, "teamId");

        this.teamId = teamId;
        this.collectedUdonTypes = new HashSet<>();
        this.dailyUdonTypesHistory = new HashMap<>();
    }

    public int getTeamId() {
        return teamId;
    }

    public Set<Integer> getCollectedUdonTypes() {
        return Collections.unmodifiableSet(collectedUdonTypes);
    }

    public Map<Integer, Set<Integer>> getDailyUdonTypesHistory() {

        Map<Integer, Set<Integer>> history = new HashMap<>();

        dailyUdonTypesHistory.forEach((turn, udonTypes) ->
                history.put(turn, Collections.unmodifiableSet(udonTypes)));

        return Collections.unmodifiableMap(history);
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

    /**
     * Returns the number of unique Udon types collected during the match.
     */
    public int getUniqueUdonTypesCount() {
        return collectedUdonTypes.size();
    }

    /**
     * Returns the accumulated number of unique Udon types collected across all turns.
     */
    public int getAccumulatedDailyUdonTypes() {
        return dailyUdonTypesHistory.values()
                .stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Increments the number of completed servings.
     */
    public void incrementServings() {
        totalServings++;
    }

    /**
     * Records a collected Udon type for the specified turn.
     */
    public void addUdonCollection(
            int turn,
            int udonType
    ) {

        requirePositive(turn, "turn");
        requireNonNegative(udonType, "udonType");

        collectedUdonTypes.add(udonType);

        dailyUdonTypesHistory
                .computeIfAbsent(turn, ignored -> new HashSet<>())
                .add(udonType);
    }

    /**
     * Adds a response time measurement.
     */
    public void addResponseTime(long durationMs) {

        requireNonNegative(durationMs, "durationMs");

        totalResponseTimeMs += durationMs;
        requestCount++;
    }

    /**
     * Returns the average response time in milliseconds.
     */
    public double getAverageResponseTimeMs() {
        return requestCount == 0
                ? 0.0
                : (double) totalResponseTimeMs / requestCount;
    }
}