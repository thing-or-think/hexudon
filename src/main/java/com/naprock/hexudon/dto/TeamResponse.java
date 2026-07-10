package com.naprock.hexudon.dto;

import com.naprock.hexudon.model.Team;

import java.util.List;

public record TeamResponse(
        String teamName,
        List<AgentResponse> agents
) {

    public TeamResponse(Team team) {
        this(
                team.getTeamName(),
                team.getAgents()
                        .stream()
                        .map(AgentResponse::new)
                        .toList()
        );
    }

    public TeamResponse {
        agents = List.copyOf(agents);
    }
}