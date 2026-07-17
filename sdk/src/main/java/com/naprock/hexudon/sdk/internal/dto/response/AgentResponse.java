package com.naprock.hexudon.sdk.internal.dto.response;

/**
 * Response DTO representing the current state of an Agent.
 *
 * <p>This DTO is used for deserializing JSON responses received from
 * the Hexudon server.</p>
 *
 * <p>Visibility: package-private.</p>
 *
 * @param kind Agent type
 *             (0 = PATROL, 1 = REFUEL)
 * @param pos Agent position represented as a linear 1D index
 * @param fuel Current fuel amount of the Agent
 */
public record AgentResponse(
        int kind,
        int pos,
        int fuel
) {

    /**
     * Compact constructor validating response values.
     */
    public AgentResponse {
        if (kind < 0) {
            throw new IllegalArgumentException(
                    "Agent kind must not be negative"
            );
        }

        if (pos < 0) {
            throw new IllegalArgumentException(
                    "Agent position must not be negative"
            );
        }

        if (fuel < 0) {
            throw new IllegalArgumentException(
                    "Agent fuel must not be negative"
            );
        }
    }
}