package com.naprock.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Response DTO containing day information from the server.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Unique identifier of the game
 * @param day Current day number
 * @param status Status of the game day (e.g. "in_progress", "waiting")
 */
public record DayInfoResponse(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("day") int day,
        @JsonProperty("status") String status
) {

    /**
     * Compact constructor validating response values.
     */
    public DayInfoResponse {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }
    }
}
