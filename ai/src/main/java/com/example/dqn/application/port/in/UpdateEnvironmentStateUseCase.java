package com.example.dqn.application.port.in;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

/**
 * Use case input port for pushing authoritative environment updates to the DQN module.
 */
public interface UpdateEnvironmentStateUseCase {
    /**
     * Updates the DQN module's state with the authoritative state.
     *
     * @param state the updated environment state.
     */
    void updateEnvironmentState(MultiAgentState state);
}
