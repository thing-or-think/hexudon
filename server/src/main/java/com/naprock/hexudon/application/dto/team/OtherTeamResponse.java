package com.naprock.hexudon.application.dto.team;

import com.naprock.hexudon.application.dto.agent.AgentResponse;

import java.util.List;

public record OtherTeamResponse(
        String id,
        List<AgentResponse> agents
) {
}