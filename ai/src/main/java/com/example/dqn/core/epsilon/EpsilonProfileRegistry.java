package com.example.dqn.core.epsilon;

import com.example.dqn.core.agent.AgentType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain registry mapping AgentType to active EpsilonProfile.
 */
public class EpsilonProfileRegistry {
    private final Map<AgentType, EpsilonProfile> registry = new ConcurrentHashMap<>();

    public void register(AgentType type, EpsilonProfile profile) {
        if (type == null || profile == null) {
            throw new IllegalArgumentException("Type and profile cannot be null");
        }
        registry.put(type, profile);
    }

    public EpsilonProfile getProfile(AgentType type) {
        if (type == null) {
            return null;
        }
        return registry.get(type);
    }
}
