package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.core.state.StateVersion;

/**
 * Thread-safe class coordinating authoritative state updates from the external environment
 * with internal predicted transition state updates. Discards stale predicted updates if new
 * authoritative updates are delivered.
 */
public class StateSynchronizer {
    private StateVersion<MultiAgentState> authoritativeState;
    private StateVersion<MultiAgentState> predictedState;
    private long versionCounter = 0;
    private boolean isNewAuthoritative = false;

    public synchronized void updateAuthoritative(MultiAgentState state) {
        versionCounter++;
        this.authoritativeState = new StateVersion<>(versionCounter, state);
        this.predictedState = null;
        this.isNewAuthoritative = true;
        this.notifyAll();
    }

    public synchronized void updatePredicted(MultiAgentState state) {
        this.predictedState = new StateVersion<>(versionCounter, state);
    }

    public synchronized MultiAgentState getAuthoritativeState() {
        return authoritativeState != null ? authoritativeState.state() : null;
    }

    public synchronized MultiAgentState getAndClearNextState() {
        if (isNewAuthoritative) {
            isNewAuthoritative = false;
            return authoritativeState.state();
        }
        if (predictedState != null) {
            MultiAgentState state = predictedState.state();
            predictedState = null; // Consume it
            return state;
        }
        return authoritativeState != null ? authoritativeState.state() : null;
    }

    public synchronized void waitForState() throws InterruptedException {
        while (authoritativeState == null && predictedState == null) {
            this.wait();
        }
    }
}
