package com.example.dqn.core.agent;

import com.example.dqn.core.state.State;

/**
 * Abstraction representing a reinforcement learning agent.
 */
public interface Agent {
    
    /**
     * Unique identifier of this agent.
     */
    AgentId id();

    /**
     * The type classification of this agent.
     */
    AgentType type();

    /**
     * The current state representation of this agent.
     */
    State state();
}
