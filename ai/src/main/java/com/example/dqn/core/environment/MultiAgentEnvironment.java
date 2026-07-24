package com.example.dqn.core.environment;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;
import java.util.Map;

/**
 * Interface representing a Multi-Agent Reinforcement Learning environment.
 */
public interface MultiAgentEnvironment {

    /**
     * Resets the environment to its initial state.
     *
     * @return the initial states map of all agents.
     */
    Map<AgentId, State> reset();

    /**
     * Returns the current states map of all agents in the environment.
     *
     * @return the current states map.
     */
    Map<AgentId, State> currentStates();

    /**
     * Represents the outcome of executing a joint step in the environment.
     */
    record MultiAgentStepResult(
        Map<AgentId, State> nextStates,
        Map<AgentId, Double> individualRewards,
        double teamReward,
        boolean done
    ) {}

    /**
     * Executes the given joint actions map for all agents in the environment,
     * transitions to the next state, and returns the result.
     *
     * @param actions the joint actions map.
     * @return the MultiAgentStepResult.
     */
    MultiAgentStepResult step(Map<AgentId, Action> actions);

    /**
     * Checks if the current episode has terminated.
     *
     * @return true if the current episode is done, false otherwise.
     */
    boolean isDone();
}
