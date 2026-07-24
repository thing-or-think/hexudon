package com.example.dqn.core.epsilon;

/**
 * Domain port interface computing exploration rate (epsilon) at a given step.
 */
public interface EpsilonSchedule {
    /**
     * Calculates the exploration rate at the given step number.
     *
     * @param step current training or episode step.
     * @return exploration probability between 0.0 and 1.0.
     */
    double epsilonAt(long step);
}
