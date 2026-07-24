package com.example.dqn.core.epsilon;

/**
 * Domain model representing a DQN exploration configuration.
 */
public class EpsilonProfile {
    private double initialEpsilon = 1.0;
    private double minimumEpsilon = 0.05;
    private double decayRate = 0.95;
    private String decayStrategy = "EXPONENTIAL"; // "EXPONENTIAL", "LINEAR", "CONSTANT", "STAGED"
    private long explorationDuration = 1000;

    public EpsilonProfile() {
        // Default constructor for Jackson
    }

    public double getInitialEpsilon() {
        return initialEpsilon;
    }

    public void setInitialEpsilon(double initialEpsilon) {
        this.initialEpsilon = initialEpsilon;
    }

    public double getMinimumEpsilon() {
        return minimumEpsilon;
    }

    public void setMinimumEpsilon(double minimumEpsilon) {
        this.minimumEpsilon = minimumEpsilon;
    }

    public double getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }

    public String getDecayStrategy() {
        return decayStrategy;
    }

    public void setDecayStrategy(String decayStrategy) {
        this.decayStrategy = decayStrategy;
    }

    public long getExplorationDuration() {
        return explorationDuration;
    }

    public void setExplorationDuration(long explorationDuration) {
        this.explorationDuration = explorationDuration;
    }
}
