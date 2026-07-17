package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the current state of a match.
 *
 * <p>A match state contains the remaining turn time, the current game day,
 * the player's agents, visible opponent agents, road traffic information,
 * and the current match status.
 *
 * @param endsAt the Unix timestamp when the current turn ends
 * @param day the current game day (zero-based)
 * @param agents the player's agents
 * @param others the visible opponent agents
 * @param traffics the current road traffic information
 * @param status the current match status
 */
public record MatchState(
        long endsAt,
        int day,
        List<Agent> agents,
        List<Agent> others,
        List<Traffic> traffics,
        MatchStatus status
) {

    /**
     * Creates a new {@code MatchState}.
     *
     * @throws NullPointerException if any required object parameter is {@code null}
     * @throws IllegalArgumentException if {@code endsAt} or {@code day} is negative
     */
    public MatchState {
        Objects.requireNonNull(agents, "agents must not be null");
        Objects.requireNonNull(others, "others must not be null");
        Objects.requireNonNull(traffics, "traffics must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (endsAt < 0) {
            throw new IllegalArgumentException("endsAt must not be negative");
        }

        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        agents = List.copyOf(agents);
        others = List.copyOf(others);
        traffics = List.copyOf(traffics);
    }
}