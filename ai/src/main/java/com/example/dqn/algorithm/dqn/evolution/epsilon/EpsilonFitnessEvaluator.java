package com.example.dqn.algorithm.dqn.evolution.epsilon;

import com.example.dqn.core.reward.TrainingStatistics;

/**
 * Calculates fitness scores for exploration profiles.
 * Strongly weights the raw stock count of Udon collected over all other metrics.
 */
public class EpsilonFitnessEvaluator {

    public double evaluate(TrainingStatistics stats) {
        if (stats == null) {
            return 0.0;
        }
        
        // Priority weights:
        double udonCountWeight = 100.0; // Most important metric: total stock collected
        double udonValueWeight = 20.0;  // Unique spots collected count
        double rewardWeight = 5.0;
        double fuelEfficiencyWeight = 2.0;
        double survivalWeight = 0.5;

        int totalUdonCollectedCount = stats.getTotalUdonCollected();
        int numberOfCollectedUdonSpots = stats.getPatrolCollectionSuccessCount();
        double episodeReward = stats.getAverageEpisodeReward();
        
        double fuelEfficiency = stats.getTotalFuelConsumed() > 0 
                ? (double) totalUdonCollectedCount / stats.getTotalFuelConsumed() 
                : 0.0;
        double survivalDuration = stats.getAverageEpisodeLength();

        return totalUdonCollectedCount * udonCountWeight
                + numberOfCollectedUdonSpots * udonValueWeight
                + episodeReward * rewardWeight
                + fuelEfficiency * fuelEfficiencyWeight
                + survivalDuration * survivalWeight;
    }
}
