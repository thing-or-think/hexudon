package com.naprock.hexudon.model;

import java.util.List;

public record AgentExecutionResult(
        String agentId,
        List<Action> actions
) {
    public AgentExecutionResult {
        actions = List.copyOf(actions);
    }
}