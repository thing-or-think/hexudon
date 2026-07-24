package com.example.dqn.algorithm.dqn.action;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.DqnAgent;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for coordinating action selection across multiple agents given a MultiAgentState.
 */
public class ActionCoordinator {
    private final AgentNetworkRegistry networkRegistry;

    public ActionCoordinator(AgentNetworkRegistry networkRegistry) {
        if (networkRegistry == null) {
            throw new IllegalArgumentException("AgentNetworkRegistry cannot be null");
        }
        this.networkRegistry = networkRegistry;
    }

    /**
     * Selects actions for all agents in the multi-agent state using their respective policies.
     *
     * @param multiState current multi-agent state.
     * @return selected actions.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<AgentAction> selectActions(MultiAgentState multiState) {
        List<AgentAction> actions = new ArrayList<>();
        if (multiState == null || multiState.agentStates() == null) {
            return actions;
        }

        multiState.agentStates().forEach((agentId, agentState) -> {
            AgentType type = agentId.type();
            DqnAgent dqnAgent = networkRegistry.agentFor(type);
            if (dqnAgent != null) {
                Action action = dqnAgent.selectAction(agentState);
                actions.add(new AgentAction(agentId, action));
            }
        });

        return actions;
    }
}
