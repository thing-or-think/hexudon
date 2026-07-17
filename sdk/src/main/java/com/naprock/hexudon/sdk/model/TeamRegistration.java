package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the information required to register a team.
 *
 * <p>A team registration contains the team identifier and the desired
 * roles for each agent.
 *
 * @param teamId the unique team identifier
 * @param types the desired agent roles
 */
public record TeamRegistration(
        String teamId,
        List<AgentType> types
) {

    /**
     * Creates a new {@code TeamRegistration}.
     *
     * @throws NullPointerException if {@code teamId} or {@code types} is {@code null}
     * @throws IllegalArgumentException if {@code teamId} is blank or
     *                                  {@code types} contains {@code null} elements
     */
    public TeamRegistration {
        Objects.requireNonNull(teamId, "teamId must not be null");
        Objects.requireNonNull(types, "types must not be null");

        if (teamId.isBlank()) {
            throw new IllegalArgumentException("teamId must not be blank");
        }

        types = List.copyOf(types);

        for (AgentType type : types) {
            if (type == null) {
                throw new IllegalArgumentException("types must not contain null elements");
            }
        }
    }
}