package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Domain registry mapping AgentType to RewardProfile.
 */
public class RewardProfileRegistry {
    private final Map<AgentType, RewardProfile> profiles = new ConcurrentHashMap<>();

    public void register(AgentType type, RewardProfile profile) {
        if (type == null || profile == null) {
            throw new IllegalArgumentException("Type and profile cannot be null");
        }
        profiles.put(type, profile);
    }

    public RewardProfile getProfile(AgentType type) {
        if (type == null) {
            return null;
        }
        return profiles.get(type);
    }
}
