package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents an agent controlled by the player's team.
 *
 * <p>An agent has a unique identifier, a role, a current position
 * on the board, and a remaining fuel amount.</p>
 *
 * @param agentId the unique agent identifier
 * @param type the agent role
 * @param coordinate the current board coordinate
 * @param fuel the remaining fuel amount
 */
public record Agent(
        String agentId,
        AgentType type,
        Coordinate coordinate,
        int fuel
) {

    /**
     * Creates a new {@code Agent}.
     *
     * @throws NullPointerException if {@code type} or {@code coordinate} is {@code null}
     * @throws IllegalArgumentException if {@code agentId} is null/blank or {@code fuel} is negative
     */
    public Agent {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be null or blank");
        }

        if (fuel < 0) {
            throw new IllegalArgumentException("fuel must not be negative");
        }
    }
}
