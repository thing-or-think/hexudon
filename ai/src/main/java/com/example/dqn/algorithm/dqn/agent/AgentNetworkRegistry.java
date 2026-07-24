package com.example.dqn.algorithm.dqn.agent;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.network.QNetwork;
import com.example.dqn.algorithm.dqn.DqnAgent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete registry class mapping AgentType to its active DqnAgent instance
 * which encapsulates the online/target networks, encoder, and action selector.
 */
public class AgentNetworkRegistry implements com.example.dqn.core.network.AgentNetworkRegistry {

    private final Map<AgentType, DqnAgent<?, ?>> registry = new ConcurrentHashMap<>();

    /**
     * Registers a DQN agent for the given agent type.
     */
    public void register(AgentType type, DqnAgent<?, ?> agent) {
        if (type == null || agent == null) {
            throw new IllegalArgumentException("Type and Agent cannot be null");
        }
        registry.put(type, agent);
    }

    @Override
    public QNetwork networkFor(AgentType agentType) {
        DqnAgent<?, ?> agent = registry.get(agentType);
        return agent != null ? agent.getOnlineNetwork() : null;
    }

    /**
     * Gets the target network for the given agent type.
     */
    public QNetwork targetNetworkFor(AgentType agentType) {
        DqnAgent<?, ?> agent = registry.get(agentType);
        return agent != null ? agent.getTargetNetwork() : null;
    }

    /**
     * Retrieves the DQN agent controller registered for the given agent type.
     */
    public DqnAgent<?, ?> agentFor(AgentType agentType) {
        return registry.get(agentType);
    }
}
