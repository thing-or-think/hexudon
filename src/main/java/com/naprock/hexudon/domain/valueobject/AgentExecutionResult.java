package com.naprock.hexudon.domain.valueobject;

import com.naprock.hexudon.domain.model.valueobject.Action;

import java.util.List;

public record AgentExecutionResult(
        String agentId,
        List<Action> actions
) {
    public AgentExecutionResult {
        actions = List.copyOf(actions);
    }
}