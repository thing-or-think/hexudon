package com.example.dqn.application.service;

import com.example.dqn.application.port.in.RequestActionsUseCase;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.algorithm.dqn.action.AgentAction;

import java.util.List;

/**
 * Service acting as entry point to pull actions selected for the current state.
 */
public class ActionRequestService implements RequestActionsUseCase {
    private final DqnTrainingSession session;

    public ActionRequestService(DqnTrainingSession session) {
        if (session == null) {
            throw new IllegalArgumentException("DqnTrainingSession cannot be null");
        }
        this.session = session;
    }

    @Override
    public List<AgentAction> requestActions() {
        return session.requestActions();
    }
}
