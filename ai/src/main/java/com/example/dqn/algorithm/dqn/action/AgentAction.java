package com.example.dqn.algorithm.dqn.action;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.action.Action;

/**
 * Immutable record representing an action decided for a specific agent.
 *
 * @param agentId unique agent identifier.
 * @param action selected action type.
 */
public record AgentAction(
    AgentId agentId,
    Action action
) {}
