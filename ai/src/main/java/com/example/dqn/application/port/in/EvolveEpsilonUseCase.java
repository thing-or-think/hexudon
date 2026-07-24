package com.example.dqn.application.port.in;

/**
 * Use case input port defining entry point for epsilon profile evolution.
 */
public interface EvolveEpsilonUseCase {
    /**
     * Executes epsilon evolution across a population of exploration configs.
     *
     * @param trainEpisodes training episodes run during selection checks.
     */
    void evolve(int trainEpisodes);
}
