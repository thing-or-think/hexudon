package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import java.util.List;

/**
 * Interface representing a stateful reinforcement learning session managing background optimization.
 */
public interface DqnTrainingSession {
    /**
     * Initializes the session, starts the background training worker, and sets the starting state.
     *
     * @param initialState starting environment state.
     */
    void initialize(MultiAgentState initialState);

    /**
     * Request joint agent actions selected for the current authoritative state.
     *
     * @return selected agent actions.
     */
    List<AgentAction> requestActions();

    /**
     * Updates the session with the authoritative external environment state.
     *
     * @param state the updated authoritative state.
     */
    void updateEnvironmentState(MultiAgentState state);

    /**
     * Retrieves the current execution status of the session.
     */
    TrainingSessionStatus status();

    /**
     * Stops the background worker and terminates the session.
     */
    void stop();
}
