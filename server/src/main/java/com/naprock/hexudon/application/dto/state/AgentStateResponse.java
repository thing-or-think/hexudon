package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO representing an agent state.
 *
 * @param agentId agent identifier
 * @param type patrol or refuel
 * @param cell current cell index
 * @param fuel remaining fuel, null for refuel agent
 */
public record AgentStateResponse(

        @JsonProperty("agent_id")
        String agentId,

        String type,

        int cell,

        Integer fuel

) {
}