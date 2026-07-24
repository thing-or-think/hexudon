package com.example.dqn.algorithm.dqn.evolution;

/**
 * Configuration class holding parameters for the reward evolution engine
 * including mutation rates, mutation magnitudes, and parameter clamping limits.
 */
public class RewardEvolutionConfig {
    private final double mutationRate;
    private final double mutationMagnitude;

    public RewardEvolutionConfig(double mutationRate, double mutationMagnitude) {
        this.mutationRate = mutationRate;
        this.mutationMagnitude = mutationMagnitude;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public double getMutationMagnitude() {
        return mutationMagnitude;
    }

    // Bounds for Patrol parameters
    public double minUdonCollectedReward() { return 0.1; }
    public double maxUdonCollectedReward() { return 100.0; }

    public double minUdonCollectionMultiplier() { return 1.0; }
    public double maxUdonCollectionMultiplier() { return 10.0; }

    public double minFuelConsumedPenalty() { return 0.0; }
    public double maxFuelConsumedPenalty() { return 50.0; }

    public double minInvalidActionPenalty() { return 0.0; }
    public double maxInvalidActionPenalty() { return 50.0; }

    public double minIdlePenalty() { return 0.0; }
    public double maxIdlePenalty() { return 10.0; }

    public double minSuccessfulCollectionBonus() { return 0.0; }
    public double maxSuccessfulCollectionBonus() { return 50.0; }

    public double minCooperationReward() { return 0.0; }
    public double maxCooperationReward() { return 50.0; }

    // Bounds for Refuel parameters
    public double minRefuelSuccessReward() { return 0.0; }
    public double maxRefuelSuccessReward() { return 50.0; }

    public double minSupportedPatrolReward() { return 0.0; }
    public double maxSupportedPatrolReward() { return 50.0; }

    public double minFuelDeliveredReward() { return 0.0; }
    public double maxFuelDeliveredReward() { return 20.0; }
}
