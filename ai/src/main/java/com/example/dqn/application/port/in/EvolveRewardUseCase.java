package com.example.dqn.application.port.in;

/**
 * Use case port for reward profile evolution.
 */
public interface EvolveRewardUseCase {
    /**
     * Executes the training and reward profile evolution cycle.
     *
     * @param trainEpisodes count of episodes to run for training.
     */
    void evolve(int trainEpisodes);
}
