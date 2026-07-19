package com.naprock.hexudon.application.dto.board;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO containing the complete board configuration.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/board</li>
 * </ul>
 */
public record BoardResponse(
        String gameId,
        boolean isPractice,
        boolean noReset,
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        MapResponse map,
        List<SpotResponse> spots,
        int fuelLimits,
        int players,
        double busyThreshold,
        double jammedThreshold,
        double agentSelectionTimeLimit
) {

    public BoardResponse {

        gameId = Objects.requireNonNull(gameId, "gameId must not be null");

        if (gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be blank");
        }

        if (startsAt <= 0) {
            throw new IllegalArgumentException("startsAt must be positive");
        }

        daySeconds = List.copyOf(
                Objects.requireNonNull(daySeconds, "daySeconds must not be null")
        );

        daySteps = List.copyOf(
                Objects.requireNonNull(daySteps, "daySteps must not be null")
        );

        map = Objects.requireNonNull(map, "map must not be null");

        spots = List.copyOf(
                Objects.requireNonNull(spots, "spots must not be null")
        );

        if (daySeconds.isEmpty()) {
            throw new IllegalArgumentException("daySeconds must not be empty");
        }

        if (daySteps.isEmpty()) {
            throw new IllegalArgumentException("daySteps must not be empty");
        }

        if (daySeconds.size() != daySteps.size()) {
            throw new IllegalArgumentException(
                    "daySeconds and daySteps must have the same size"
            );
        }

        for (Double second : daySeconds) {
            if (second == null || second <= 0) {
                throw new IllegalArgumentException(
                        "Each value in daySeconds must be greater than 0"
                );
            }
        }

        for (Integer step : daySteps) {
            if (step == null || step <= 0) {
                throw new IllegalArgumentException(
                        "Each value in daySteps must be greater than 0"
                );
            }
        }

        if (fuelLimits <= 0) {
            throw new IllegalArgumentException("fuelLimits must be positive");
        }

        if (players <= 0) {
            throw new IllegalArgumentException("players must be positive");
        }

        if (busyThreshold <= 0) {
            throw new IllegalArgumentException(
                    "busyThreshold must be greater than 0"
            );
        }

        if (jammedThreshold <= busyThreshold) {
            throw new IllegalArgumentException(
                    "jammedThreshold must be greater than busyThreshold"
            );
        }

        if (agentSelectionTimeLimit <= 0) {
            throw new IllegalArgumentException(
                    "agentSelectionTimeLimit must be greater than 0"
            );
        }
    }
}