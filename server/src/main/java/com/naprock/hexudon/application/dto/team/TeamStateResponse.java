package com.naprock.hexudon.application.dto.team;

import com.naprock.hexudon.application.dto.agent.AgentResponse;

import java.util.List;

public record TeamStateResponse(
        String teamId,
        TeamDetailResponse score,
        List<AgentResponse> agents
) {
}