package com.naprock.hexudon.dto;

import java.util.List;

public record DayActionResponse(
        int day,
        List<AgentActionPlanResponse> agentPlans
) {

    public DayActionResponse {
        agentPlans = List.copyOf(agentPlans);
    }
}