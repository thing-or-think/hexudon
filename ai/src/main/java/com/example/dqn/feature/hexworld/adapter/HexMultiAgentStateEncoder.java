package com.example.dqn.feature.hexworld.adapter;

import com.example.dqn.core.state.State;
import com.example.dqn.core.state.StateEncoder;
import com.example.dqn.feature.hexworld.domain.state.PatrolState;
import com.example.dqn.feature.hexworld.domain.state.RefuelState;

/**
 * Multiplexing StateEncoder that inspects the runtime type of the state
 * and delegates to the appropriate specialized state encoder.
 */
public class HexMultiAgentStateEncoder implements StateEncoder<State> {

    private final PatrolStateEncoder patrolEncoder = new PatrolStateEncoder();
    private final RefuelStateEncoder refuelEncoder = new RefuelStateEncoder();

    @Override
    public float[] encode(State state) {
        if (state instanceof PatrolState patrolState) {
            return patrolEncoder.encode(patrolState);
        } else if (state instanceof RefuelState refuelState) {
            return refuelEncoder.encode(refuelState);
        } else {
            throw new IllegalArgumentException("Unsupported state type for encoding: " + 
                    (state == null ? "null" : state.getClass().getName()));
        }
    }
}
