package com.naprock.hexudon.sdk.model.response;

/**
 * Response DTO containing the state of an agent.
 *
 * @param kind the agent type (0 = PATROL, 1 = REFUEL)
 * @param pos the current linear position of the agent on the map
 * @param fuel the remaining fuel of the agent
 */
public record AgentResponse(
        int kind,
        int pos,
        int fuel
) {

    /**
     * Creates a new {@code AgentResponse}.
     *
     * @throws IllegalArgumentException if {@code kind}, {@code pos}, or {@code fuel} is negative
     */
    public AgentResponse {
        if (kind < 0) {
            throw new IllegalArgumentException("kind must not be negative");
        }

        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (fuel < 0) {
            throw new IllegalArgumentException("fuel must not be negative");
        }
    }
}