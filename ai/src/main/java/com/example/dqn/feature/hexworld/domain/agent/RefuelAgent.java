package com.example.dqn.feature.hexworld.domain.agent;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.HexPosition;

/**
 * Concrete agent representing a Refuel Agent whose job is to travel to Patrol Agents
 * and replenish their fuel supplies. Refuel agents do not consume fuel themselves.
 */
public class RefuelAgent extends HexAgent {

    public RefuelAgent(AgentId id, HexPosition position) {
        super(id, position);
    }

    @Override
    public AgentType type() {
        return AgentType.REFUEL;
    }

    @Override
    public State state() {
        // State is constructed via Environment and StateEncoders.
        return null;
    }
}
