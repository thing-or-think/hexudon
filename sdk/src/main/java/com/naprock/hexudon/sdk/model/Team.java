package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the player's team.
 *
 * <p>A team consists of a unique team identifier and the agents
 * controlled by the player.
 *
 * @param teamId the unique team identifier
 * @param agents the agents belonging to the team
 */
public record Team(
        String teamId,
        List<Agent> agents
) {

    /**
     * Creates a new {@code Team}.
     *
     * @throws NullPointerException if {@code teamId} or {@code agents} is {@code null}
     * @throws IllegalArgumentException if {@code teamId} is blank
     */
    public Team {
        Objects.requireNonNull(teamId, "teamId must not be null");
        Objects.requireNonNull(agents, "agents must not be null");

        if (teamId.isBlank()) {
            throw new IllegalArgumentException("teamId must not be blank");
        }

        agents = List.copyOf(agents);
    }
}