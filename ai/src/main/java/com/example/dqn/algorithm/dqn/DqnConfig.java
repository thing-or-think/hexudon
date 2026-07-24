package com.example.dqn.algorithm.dqn;

/**
 * Configuration parameters for the DQN Agent and Training process.
 * Using a Java record for immutability.
 *
 * @param stateDimension the number of inputs to the Q-network (state features count).
 * @param actionSpaceSize the number of outputs from the Q-network (discrete actions count).
 * @param hiddenLayers specification of hidden layer nodes, e.g. [128, 128].
 * @param learningRate Adam optimizer learning rate.
 * @param gamma the future discount factor (typically between 0.9 and 0.999).
 * @param batchSize size of training mini-batches sampled from the replay buffer.
 * @param replayCapacity maximum capacity of the experience replay buffer.
 * @param epsilonStart initial exploration rate (usually 1.0).
 * @param epsilonMin minimum exploration rate (usually 0.01 - 0.1).
 * @param epsilonDecay decay factor applied to epsilon after each episode.
 * @param targetUpdateFrequency target network synchronization interval (measured in steps).
 */
public record DqnConfig(
    int stateDimension,
    int actionSpaceSize,
    int[] hiddenLayers,
    double learningRate,
    double gamma,
    int batchSize,
    int replayCapacity,
    double epsilonStart,
    double epsilonMin,
    double epsilonDecay,
    int targetUpdateFrequency
) {
    public DqnConfig {
        if (stateDimension <= 0) throw new IllegalArgumentException("stateDimension must be positive");
        if (actionSpaceSize <= 0) throw new IllegalArgumentException("actionSpaceSize must be positive");
        if (learningRate <= 0) throw new IllegalArgumentException("learningRate must be positive");
        if (gamma < 0 || gamma > 1) throw new IllegalArgumentException("gamma must be between 0 and 1");
        if (batchSize <= 0) throw new IllegalArgumentException("batchSize must be positive");
        if (replayCapacity <= 0) throw new IllegalArgumentException("replayCapacity must be positive");
        if (epsilonStart < 0 || epsilonStart > 1) throw new IllegalArgumentException("epsilonStart must be between 0 and 1");
        if (epsilonMin < 0 || epsilonMin > 1) throw new IllegalArgumentException("epsilonMin must be between 0 and 1");
        if (epsilonDecay <= 0 || epsilonDecay > 1) throw new IllegalArgumentException("epsilonDecay must be between 0 and 1");
        if (targetUpdateFrequency <= 0) throw new IllegalArgumentException("targetUpdateFrequency must be positive");
    }
}
