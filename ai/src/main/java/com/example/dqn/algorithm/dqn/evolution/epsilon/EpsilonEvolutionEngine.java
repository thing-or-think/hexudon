package com.example.dqn.algorithm.dqn.evolution.epsilon;

import com.example.dqn.core.reward.TrainingStatistics;

/**
 * Coordinated engine that manages candidate EpsilonProfile evaluation and GA selection steps.
 */
public class EpsilonEvolutionEngine {
    private final EpsilonMutationStrategy mutationStrategy;
    private final EpsilonFitnessEvaluator fitnessEvaluator;
    private final EpsilonEvolutionConfig config;

    public EpsilonEvolutionEngine(
            EpsilonMutationStrategy mutationStrategy,
            EpsilonFitnessEvaluator fitnessEvaluator,
            EpsilonEvolutionConfig config
    ) {
        this.mutationStrategy = mutationStrategy;
        this.fitnessEvaluator = fitnessEvaluator;
        this.config = config;
    }

    public double evaluateFitness(TrainingStatistics statistics) {
        return fitnessEvaluator.evaluate(statistics);
    }

    public EpsilonMutationStrategy getMutationStrategy() {
        return mutationStrategy;
    }

    public EpsilonFitnessEvaluator getFitnessEvaluator() {
        return fitnessEvaluator;
    }

    public EpsilonEvolutionConfig getConfig() {
        return config;
    }
}
