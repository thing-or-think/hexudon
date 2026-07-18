package com.thingorthink.hexudon.sdk.model;

import java.util.Objects;

/**
 * Domain model containing current game day synchronization info.
 *
 * @param gameId Unique identifier of the game
 * @param day Current game day (zero-based)
 * @param status Status of the game day
 */
public record DayInfo(
        String gameId,
        int day,
        String status
) {

    /**
     * Compact constructor validating day info data.
     */
    public DayInfo {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be null or blank");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be null or blank");
        }
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }
    }
}
