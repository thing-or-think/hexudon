package com.example.dqn.application.service;

import com.example.dqn.application.port.in.UpdateEnvironmentStateUseCase;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

/**
 * Service for pushing authoritative environment updates down to the stateful training session.
 */
public class EnvironmentStateUpdateService implements UpdateEnvironmentStateUseCase {
    private final DqnTrainingSession session;

    public EnvironmentStateUpdateService(DqnTrainingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("DqnTrainingSession cannot be null");
        }
        this.session = session;
    }

    @Override
    public void updateEnvironmentState(MultiAgentState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        session.updateEnvironmentState(state);
    }
}
