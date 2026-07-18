package com.naprock.hexudon.sdk.model;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the dynamic state of a match.
 *
 * @param endsAt the Unix timestamp when the current turn ends
 * @param day the current game day (zero-based)
 * @param stepsToday steps allowed today
 * @param roadCondition traffic congestion level map of the road coordinates
 * @param teams active teams participating in the game indexed by team ID
 * @param status current match status
 */
public record MatchState(
        long endsAt,
        int day,
        int stepsToday,
        Map<Coordinate, TrafficLevel> roadCondition,
        Map<String, Team> teams,
        MatchStatus status
) {

    /**
     * Compact constructor validating match state data.
     */
    public MatchState {
        Objects.requireNonNull(roadCondition, "roadCondition must not be null");
        Objects.requireNonNull(teams, "teams must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (endsAt < 0) {
            throw new IllegalArgumentException("endsAt must not be negative");
        }

        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        if (stepsToday < 0) {
            throw new IllegalArgumentException("stepsToday must not be negative");
        }

        roadCondition = Map.copyOf(roadCondition);
        teams = Map.copyOf(teams);
    }
}
