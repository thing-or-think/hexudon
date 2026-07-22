package com.naprock.hexudon.domain.model.score;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public class TeamScore {

    private final String teamId;
    private final Set<Integer> collectedUdonTypes;
    private final Map<Integer, Set<Integer>> dailyUdonTypesHistory;

    private int totalServings;
    private long totalResponseTimeMs;
    private int requestCount;

    public TeamScore(String teamId) {

        requireNotBlank(teamId, "teamId");

        this.teamId = teamId;
        this.collectedUdonTypes = new HashSet<>();
        this.dailyUdonTypesHistory = new HashMap<>();
    }

    public void incrementServings() {
        totalServings++;
    }

    public void addUdonCollection(
            int day,
            int udonType
    ) {

        requirePositive(day, "day");
        requireNonNegative(udonType, "udonType");

        collectedUdonTypes.add(udonType);

        dailyUdonTypesHistory
                .computeIfAbsent(day, ignored -> new HashSet<>())
                .add(udonType);
    }

    public void addResponseTime(long durationMs) {

        requireNonNegative(durationMs, "durationMs");

        totalResponseTimeMs += durationMs;
        requestCount++;
    }

    public String getTeamId() {
        return teamId;
    }

    public Set<Integer> getCollectedUdonTypes() {
        return Collections.unmodifiableSet(collectedUdonTypes);
    }

    public Map<Integer, Set<Integer>> getDailyUdonTypesHistory() {

        Map<Integer, Set<Integer>> history = new HashMap<>();

        dailyUdonTypesHistory.forEach((day, udonTypes) ->
                history.put(day, Collections.unmodifiableSet(udonTypes)));

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

    public int getUniqueUdonTypesCount() {
        return collectedUdonTypes.size();
    }

    public int getAccumulatedDailyUdonTypes() {
        return dailyUdonTypesHistory.values()
                .stream()
                .mapToInt(Set::size)
                .sum();
    }

    public double getAverageResponseTimeMs() {
        return requestCount == 0
                ? 0.0
                : (double) totalResponseTimeMs / requestCount;
    }
}