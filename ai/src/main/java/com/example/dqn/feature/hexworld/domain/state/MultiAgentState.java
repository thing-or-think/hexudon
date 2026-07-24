package com.example.dqn.feature.hexworld.domain.state;

import com.example.dqn.core.state.State;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.feature.hexworld.domain.HexPosition;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * State representing a collection of individual agent states and collected Udon coordinates.
 */
public record MultiAgentState(
    Map<AgentId, State> agentStates,
    Set<HexPosition> collectedUdonSpots
) implements State {

    public MultiAgentState(Map<AgentId, State> agentStates) {
        this(agentStates, Collections.emptySet());
    }

    public MultiAgentState {
        if (agentStates == null) {
            agentStates = Collections.emptyMap();
        }
        if (collectedUdonSpots == null) {
            collectedUdonSpots = Collections.emptySet();
        }
    }
}
