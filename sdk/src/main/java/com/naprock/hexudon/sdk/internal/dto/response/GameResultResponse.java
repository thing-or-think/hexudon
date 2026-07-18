package com.naprock.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Response DTO containing final game result from the server.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Unique identifier of the game
 * @param winner Team ID of the winner
 * @param scores Map of scores (team ID -> score)
 * @param finishedAt ISO-8601 string representation of game end time
 */
public record GameResultResponse(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("winner") String winner,
        @JsonProperty("scores") Map<String, Integer> scores,
        @JsonProperty("finished_at") String finishedAt
) {

    /**
     * Compact constructor validating response values.
     */
    public GameResultResponse {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(winner, "winner must not be null");
        Objects.requireNonNull(scores, "scores must not be null");
        Objects.requireNonNull(finishedAt, "finishedAt must not be null");

        scores = Map.copyOf(scores);
    }
}
