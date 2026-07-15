package com.naprock.hexudon.application.dto.team;

import com.naprock.hexudon.application.dto.agent.AgentResponse;

import java.util.List;
import java.util.Objects;

public record TeamResponse(
        int id,
        List<AgentResponse> agents
) {

    public TeamResponse {
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }

        Objects.requireNonNull(
                agents,
                "agents must not be null"
        );

        if (agents.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "agents must not contain null element"
            );
        }

        agents = List.copyOf(agents);
    }
}