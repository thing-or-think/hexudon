package com.example.dqn.application.port.in;

import com.example.dqn.algorithm.dqn.action.AgentAction;
import java.util.List;

/**
 * Use case input port for requesting actions decided for the current authoritative state.
 */
public interface RequestActionsUseCase {
    /**
     * Request joint agent actions.
     */
    List<AgentAction> requestActions();
}
