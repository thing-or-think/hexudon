package com.example.dqn.algorithm.dqn.evolution.epsilon;

/**
 * Configurations holding parameters for the Genetic Algorithm search of Epsilon exploration profiles.
 */
public class EpsilonEvolutionConfig {
    private final int populationSize;
    private final int generations;
    private final double mutationRate;
    private final double mutationStrength;
    private final int eliteCount;
    private final int evaluationEpisodes;
    private final long seed;

    public EpsilonEvolutionConfig(
            int populationSize,
            int generations,
            double mutationRate,
            double mutationStrength,
            int eliteCount,
            int evaluationEpisodes,
            long seed
    ) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.mutationStrength = mutationStrength;
        this.eliteCount = eliteCount;
        this.evaluationEpisodes = evaluationEpisodes;
        this.seed = seed;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getGenerations() {
        return generations;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public double getMutationStrength() {
        return mutationStrength;
    }

    public int getEliteCount() {
        return eliteCount;
    }

    public int getEvaluationEpisodes() {
        return evaluationEpisodes;
    }

    public long getSeed() {
        return seed;
    }
}
