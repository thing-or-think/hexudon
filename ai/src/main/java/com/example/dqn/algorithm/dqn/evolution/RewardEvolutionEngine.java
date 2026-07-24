package com.example.dqn.algorithm.dqn.evolution;

import com.example.dqn.core.reward.RewardProfileContainer;
import com.example.dqn.core.reward.TrainingStatistics;

/**
 * Main coordinator for calculating fitness, performing mutation, and comparing profile performances.
 */
public class RewardEvolutionEngine {
    private final RewardMutationStrategy mutationStrategy;
    private final RewardFitnessEvaluator fitnessEvaluator;
    private final RewardEvolutionConfig config;

    public RewardEvolutionEngine(
            RewardMutationStrategy mutationStrategy,
            RewardFitnessEvaluator fitnessEvaluator,
            RewardEvolutionConfig config
    ) {
        this.mutationStrategy = mutationStrategy;
        this.fitnessEvaluator = fitnessEvaluator;
        this.config = config;
    }

    public RewardProfileContainer mutate(RewardProfileContainer current) {
        return mutationStrategy.mutate(current, config);
    }

    public double evaluateFitness(TrainingStatistics statistics) {
        return fitnessEvaluator.evaluate(statistics);
    }

    public RewardEvolutionResult evolve(
            RewardProfileContainer current,
            double currentFitness,
            RewardProfileContainer candidate,
            double candidateFitness
    ) {
        if (candidateFitness > currentFitness) {
            // Evolved successfully! Increase generation number
            RewardProfileContainer evolvedContainer = new RewardProfileContainer(
                    current.getVersion(),
                    current.getGeneration() + 1,
                    candidate.getProfiles()
            );
            return new RewardEvolutionResult(true, currentFitness, candidateFitness, evolvedContainer);
        } else {
            // Revert/keep current
            return new RewardEvolutionResult(false, currentFitness, candidateFitness, current);
        }
    }

    public RewardEvolutionConfig getConfig() {
        return config;
    }
}
