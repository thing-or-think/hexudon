package com.example.dqn.core.reward;

/**
 * Data structure representing a single component of calculated reward.
 */
public class RewardSignal {
    private final RewardType type;
    private final double value;

    public RewardSignal(RewardType type, double value) {
        this.type = type;
        this.value = value;
    }

    public RewardType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }
}
