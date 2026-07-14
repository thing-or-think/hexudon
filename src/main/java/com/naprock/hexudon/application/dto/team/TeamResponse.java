package com.naprock.hexudon.application.dto.team;

import com.naprock.hexudon.application.dto.agent.AgentResponse;

import java.util.List;
import java.util.Objects;

public record TeamResponse(
        String teamName,
        List<AgentResponse> agents
) {

    public TeamResponse {
        Objects.requireNonNull(teamName, "teamName must not be null");
        Objects.requireNonNull(agents, "agents must not be null");

        if (teamName.isBlank()) {
            throw new IllegalArgumentException("teamName must not be blank");
        }

        if (agents.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("agents must not contain null element");
        }

        agents = List.copyOf(agents);
    }
}