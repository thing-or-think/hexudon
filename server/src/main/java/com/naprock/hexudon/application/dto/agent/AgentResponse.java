package com.naprock.hexudon.application.dto.agent;

public record AgentResponse(
        int kind,
        int pos,
        int fuel
) {

    public AgentResponse {
        if (kind != 0 && kind != 1) {
            throw new IllegalArgumentException("kind must be either 0 (PATROL) or 1 (REFUEL)");
        }

        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (fuel < 0) {
            throw new IllegalArgumentException("fuel must not be negative");
        }
    }
}