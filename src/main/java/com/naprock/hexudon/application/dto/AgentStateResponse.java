package com.naprock.hexudon.application.dto;

import java.util.Objects;

/**
 * Response DTO representing the runtime state of an Agent.
 *
 * @param agentId    unique identifier of the agent
 * @param teamName   owner team name
 * @param coordinate current coordinate
 * @param fuel       remaining fuel
 */
public record AgentStateResponse(
        String agentId,
        String teamName,
        CoordinateResponse coordinate,
        int fuel
) {

    public AgentStateResponse {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(teamName, "teamName must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");
    }
}