package com.example.dqn.application.port.in;

/**
 * Input port for training a reinforcement learning agent.
 */
public interface TrainAgentUseCase {

    /**
     * Executes the training phase for a specified number of episodes.
     *
     * @param episodes the number of episodes to train.
     */
    void train(int episodes);
}
