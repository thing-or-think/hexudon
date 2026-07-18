package com.thingorthink.hexudon.sdk.model;

import java.util.Map;
import java.util.Objects;

/**
 * Domain model containing final match results.
 *
 * @param gameId Unique identifier of the game
 * @param winner Winning team identifier
 * @param scores Map of scores (teamId -> score)
 * @param finishedAt Ending timestamp as ISO-8601 string
 */
public record GameResult(
        String gameId,
        String winner,
        Map<String, Integer> scores,
        String finishedAt
) {

    /**
     * Compact constructor validating game result data.
     */
    public GameResult {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be null or blank");
        }
        if (winner == null || winner.isBlank()) {
            throw new IllegalArgumentException("winner must not be null or blank");
        }
        Objects.requireNonNull(scores, "scores must not be null");
        if (finishedAt == null || finishedAt.isBlank()) {
            throw new IllegalArgumentException("finishedAt must not be null or blank");
        }

        scores = Map.copyOf(scores);
    }
}
