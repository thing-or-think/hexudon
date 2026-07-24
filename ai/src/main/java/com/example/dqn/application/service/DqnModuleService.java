package com.example.dqn.application.service;

import com.example.dqn.application.port.in.InitializeDqnModuleUseCase;
import com.example.dqn.application.port.in.StopDqnModuleUseCase;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

/**
 * Service orchestrating initialization and termination of the DQN module session.
 */
public class DqnModuleService implements InitializeDqnModuleUseCase, StopDqnModuleUseCase {
    private final DqnTrainingSession session;

    public DqnModuleService(DqnTrainingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("DqnTrainingSession cannot be null");
        }
        this.session = session;
    }

    @Override
    public void initialize(MultiAgentState initialState) {
        if (initialState == null || initialState.agentStates() == null || initialState.agentStates().isEmpty()) {
            throw new IllegalArgumentException("Initial state cannot be null or empty");
        }
        session.initialize(initialState);
    }

    @Override
    public void stop() {
        session.stop();
    }
}
