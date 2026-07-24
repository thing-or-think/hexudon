package com.example.dqn.algorithm.dqn;

import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

import java.util.List;

/**
 * Façade wrapping the stateful DQN module session.
 */
public class DqnModule {
    private final DqnTrainingSession session;

    public DqnModule(DqnTrainingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("DqnTrainingSession cannot be null");
        }
        this.session = session;
    }

    /**
     * Initializes the session with the initial state and starts background training.
     */
    public void initialize(MultiAgentState initialState) {
        session.initialize(initialState);
    }

    /**
     * Requests actions selected for the current state.
     */
    public List<AgentAction> requestActions() {
        return session.requestActions();
    }

    /**
     * Updates the session with the authoritative external environment state.
     */
    public void updateEnvironmentState(MultiAgentState state) {
        session.updateEnvironmentState(state);
    }

    /**
     * Stops background training and stops the session.
     */
    public void stop() {
        session.stop();
    }

    public DqnTrainingSession getSession() {
        return session;
    }
}
