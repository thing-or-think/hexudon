package com.example.dqn.application.port.in;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

/**
 * Use case input port for initializing the stateful DQN module.
 */
public interface InitializeDqnModuleUseCase {
    /**
     * Initializes the DQN Module with the initial state and resumes training thread.
     *
     * @param initialState starting environment state.
     */
    void initialize(MultiAgentState initialState);
}
