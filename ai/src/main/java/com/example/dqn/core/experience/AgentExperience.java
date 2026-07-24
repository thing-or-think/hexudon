package com.example.dqn.core.experience;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;

/**
 * An immutable record representing an individual agent's experience transition tuple (s, a, r, s', done)
 * along with the AgentType for experience replay separation.
 */
public record AgentExperience(
    AgentId agentId,
    AgentType agentType,
    State state,
    Action action,
    double reward,
    State nextState,
    boolean done
) {}
