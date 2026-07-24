package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

/**
 * Thread-safe wrapper holding dynamic state variables of a running DqnTrainingSession.
 */
public class TrainingSessionState {
    private final long startTime;
    private long trainingSteps;
    private MultiAgentState lastState;

    public TrainingSessionState() {
        this.startTime = System.currentTimeMillis();
        this.trainingSteps = 0;
    }

    public long getStartTime() {
        return startTime;
    }

    public synchronized long getTrainingSteps() {
        return trainingSteps;
    }

    public synchronized void incrementTrainingSteps() {
        this.trainingSteps++;
    }

    public synchronized MultiAgentState getLastState() {
        return lastState;
    }

    public synchronized void setLastState(MultiAgentState lastState) {
        this.lastState = lastState;
    }
}
