package com.example.dqn.algorithm.dqn.evolution;

import com.example.dqn.core.reward.TrainingStatistics;

/**
 * Calculates a fitness score from training cycle statistics.
 * Rewards successful Udon collection and coordination, penalizes fuel waste and invalid moves.
 */
public class RewardFitnessEvaluator {

    public double evaluate(TrainingStatistics stats) {
        double udonWeight = 20.0;
        double completionWeight = 50.0;
        double refuelWeight = 10.0;
        double cooperationWeight = 15.0;
        double fuelEfficiencyPenalty = 0.5;
        double invalidActionPenalty = 5.0;

        double fitness = stats.getTotalUdonCollected() * udonWeight
                + stats.getCompletedEpisodes() * completionWeight
                + stats.getTotalRefuelSuccess() * refuelWeight
                + stats.getTotalSupportedPatrols() * cooperationWeight
                - stats.getTotalFuelConsumed() * fuelEfficiencyPenalty
                - stats.getTotalInvalidActions() * invalidActionPenalty;

        return fitness;
    }
}
