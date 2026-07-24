package com.example.dqn.feature.hexworld.domain.agent;

import com.example.dqn.core.agent.AgentType;

/**
 * Domain-specific enum mapping HexWorld agent types to core agent types.
 */
public enum HexAgentType {
    PATROL(AgentType.PATROL),
    REFUEL(AgentType.REFUEL);

    private final AgentType coreType;

    HexAgentType(AgentType coreType) {
        this.coreType = coreType;
    }

    public AgentType coreType() {
        return coreType;
    }
}
