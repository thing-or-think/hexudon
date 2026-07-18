package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the static configuration of a match.
 *
 * <p>A match configuration contains the board definition, game parameters,
 * Udon shop locations, and the initial positions of all agents.
 *
 * @param startsAt the Unix timestamp when the match starts
 * @param daySeconds the duration of each game day in seconds
 * @param daySteps the maximum number of movement steps allowed for each day
 * @param mapHeight the board height
 * @param mapWidth the board width
 * @param board the game board
 * @param spots the Udon shop locations
 * @param agentsStartPos the starting positions of all agents
 * @param fuelLimits the maximum fuel capacity for each agent
 * @param playersLimit the maximum number of teams
 * @param busyThreshold the threshold for the {@link TrafficLevel#BUSY} traffic level
 * @param jammedThreshold the threshold for the {@link TrafficLevel#CONGESTED} traffic level
 */
public record MatchConfig(
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        int mapHeight,
        int mapWidth,
        Board board,
        List<Spot> spots,
        List<Coordinate> agentsStartPos,
        int fuelLimits,
        int playersLimit,
        double busyThreshold,
        double jammedThreshold
) {

    /**
     * Creates a new {@code MatchConfig}.
     *
     * @throws NullPointerException if any required object parameter is {@code null}
     * @throws IllegalArgumentException if any numeric constraint is violated
     */
    public MatchConfig {
        Objects.requireNonNull(daySeconds, "daySeconds must not be null");
        Objects.requireNonNull(daySteps, "daySteps must not be null");
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(spots, "spots must not be null");
        Objects.requireNonNull(agentsStartPos, "agentsStartPos must not be null");

        if (startsAt < 0) {
            throw new IllegalArgumentException("startsAt must not be negative");
        }

        if (mapHeight <= 0) {
            throw new IllegalArgumentException("mapHeight must be greater than 0");
        }

        if (mapWidth <= 0) {
            throw new IllegalArgumentException("mapWidth must be greater than 0");
        }

        if (fuelLimits < 0) {
            throw new IllegalArgumentException("fuelLimits must not be negative");
        }

        if (playersLimit <= 0) {
            throw new IllegalArgumentException("players must be greater than 0");
        }

        if (busyThreshold < 0.0) {
            throw new IllegalArgumentException("busyThreshold must not be negative");
        }

        if (jammedThreshold < 0.0) {
            throw new IllegalArgumentException("jammedThreshold must not be negative");
        }

        daySeconds = List.copyOf(daySeconds);
        daySteps = List.copyOf(daySteps);
        spots = List.copyOf(spots);
        agentsStartPos = List.copyOf(agentsStartPos);
    }
}
