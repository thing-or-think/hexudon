package com.naprock.hexudon.sdk.model.response;

import java.util.List;

/**
 * Response DTO containing the complete match configuration.
 *
 * @param startsAt the Unix timestamp when the match starts
 * @param daySeconds the duration of each game day in seconds
 * @param daySteps the maximum number of movement steps allowed for each day
 * @param mapHeight the height of the game map
 * @param mapWidth the width of the game map
 * @param cells the two-dimensional terrain map
 * @param spots the list of Udon spots
 * @param agentsStartPos the initial positions of all agents
 * @param fuelLimits the maximum fuel capacity
 * @param playersLimit the maximum number of teams
 * @param busyThreshold the threshold for BUSY traffic
 * @param jammedThreshold the threshold for CONGESTED traffic
 */
public record MatchConfigResponse(
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        int mapHeight,
        int mapWidth,
        List<List<Integer>> cells,
        List<SpotResponse> spots,
        List<Integer> agentsStartPos,
        int fuelLimits,
        int playersLimit,
        double busyThreshold,
        double jammedThreshold
) {

    /**
     * Creates a new {@code MatchConfigResponse}.
     * <p>
     * Any {@code null} collection is replaced with an empty immutable list.
     * Nested collections are also copied into immutable lists.
     */
    public MatchConfigResponse {
        daySeconds = daySeconds == null
                ? List.of()
                : List.copyOf(daySeconds);

        daySteps = daySteps == null
                ? List.of()
                : List.copyOf(daySteps);

        cells = cells == null
                ? List.of()
                : cells.stream()
                .map(row -> row == null ? List.<Integer>of() : List.copyOf(row))
                .toList();

        spots = spots == null
                ? List.of()
                : List.copyOf(spots);

        agentsStartPos = agentsStartPos == null
                ? List.of()
                : List.copyOf(agentsStartPos);
    }
}