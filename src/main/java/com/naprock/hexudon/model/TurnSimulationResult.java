package com.naprock.hexudon.model;

import java.util.List;

public record TurnSimulationResult(
        int day,
        List<AgentExecutionResult> agentExecutionResults
) {

    public TurnSimulationResult {
        agentExecutionResults = List.copyOf(agentExecutionResults);
    }
}