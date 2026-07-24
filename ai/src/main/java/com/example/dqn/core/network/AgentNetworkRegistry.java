package com.example.dqn.core.network;

import com.example.dqn.core.agent.AgentType;

/**
 * Abstraction mapping AgentType to its online QNetwork.
 */
public interface AgentNetworkRegistry {

    /**
     * Resolves the active QNetwork for a given AgentType.
     *
     * @param agentType the type of the agent.
     * @return the QNetwork.
     */
    QNetwork networkFor(AgentType agentType);
}
