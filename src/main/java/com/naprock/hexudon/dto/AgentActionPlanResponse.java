package com.naprock.hexudon.dto;

import java.util.List;

public record AgentActionPlanResponse(
        String agentId,
        List<ActionResponse> actions
) {
    public AgentActionPlanResponse {
        actions = List.copyOf(actions);
    }
}