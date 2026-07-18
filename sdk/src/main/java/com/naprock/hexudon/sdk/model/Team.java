package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a player's team in the game.
 *
 * <p>A team contains its unique identifier, the agents controlled by
 * the player, and the list of distinct shop brands the team has visited.</p>
 *
 * @param teamId the unique team identifier
 * @param agents the agents belonging to the team
 * @param distinctBrands the distinct shop brands visited by the team
 */
public record Team(
        String teamId,
        List<Agent> agents,
        List<String> distinctBrands
) {

    /**
     * Creates a new {@code Team}.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code teamId} is blank
     */
    public Team {
        Objects.requireNonNull(teamId, "teamId must not be null");
        Objects.requireNonNull(agents, "agents must not be null");
        Objects.requireNonNull(distinctBrands, "distinctBrands must not be null");

        if (teamId.isBlank()) {
            throw new IllegalArgumentException("teamId must not be blank");
        }

        agents = List.copyOf(agents);
        distinctBrands = List.copyOf(distinctBrands);
    }
}
