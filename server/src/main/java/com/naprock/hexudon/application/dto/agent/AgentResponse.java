package com.naprock.hexudon.application.dto.agent;

import com.naprock.hexudon.application.dto.match.CoordinateResponse;
import com.naprock.hexudon.domain.model.agent.AgentType;

import java.util.Objects;

public record AgentResponse(
        String agentId,
        CoordinateResponse coordinate,
        AgentType agentType,
        int fuel,
        int step
) {

    public AgentResponse {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(agentType, "agentType must not be null");

        if (fuel < 0) {
            throw new IllegalArgumentException("fuel must not be negative");
        }

        if (step < 0) {
            throw new IllegalArgumentException("step must not be negative");
        }
    }
}