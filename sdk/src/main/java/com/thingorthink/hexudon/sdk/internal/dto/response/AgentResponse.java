package com.thingorthink.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Response DTO representing the state of an Agent.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param agentId Unique identifier of the agent
 * @param pos Agent position represented as flat 1D index
 * @param cell Alternative field for position
 * @param fuel Fuel remaining
 * @param type Agent type/role (integer code or string like "patrol"/"refuel")
 * @param kind Alternative field for role
 */
public record AgentResponse(
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("pos") Integer pos,
        @JsonProperty("cell") Integer cell,
        @JsonProperty("fuel") int fuel,
        @JsonProperty("type") Object type,
        @JsonProperty("kind") Integer kind
) {

    /**
     * Compact constructor validating response values.
     */
    public AgentResponse {
        Objects.requireNonNull(agentId, "Agent ID must not be null");
        if (fuel < 0) {
            throw new IllegalArgumentException("Fuel must not be negative");
        }
    }
}
