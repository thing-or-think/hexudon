package com.example.dqn.algorithm.dqn.training;

/**
 * A batch of experiences sampled from the replay buffer, structured in matrix form
 * for efficient parallel batch calculations in the neural network.
 *
 * @param states states of shape [batchSize, stateDimension]
 * @param actions action indices of shape [batchSize]
 * @param rewards rewards of shape [batchSize]
 * @param nextStates next states of shape [batchSize, stateDimension]
 * @param dones terminal state flags of shape [batchSize]
 */
public record TrainingBatch(
    float[][] states,
    int[] actions,
    float[] rewards,
    float[][] nextStates,
    boolean[] dones
) {
    /**
     * Returns the batch size (number of experience samples in this batch).
     *
     * @return batch size.
     */
    public int size() {
        return states.length;
    }
}
