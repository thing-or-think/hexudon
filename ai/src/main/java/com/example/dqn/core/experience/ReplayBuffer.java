package com.example.dqn.core.experience;

import com.example.dqn.algorithm.dqn.training.TrainingBatch;

/**
 * Interface representing the Experience Replay Buffer.
 * Stores experiences and provides random mini-batches of transitions to train the DQN.
 */
public interface ReplayBuffer {

    /**
     * Adds an experience to the buffer.
     * If the buffer is full, the oldest experience should be evicted (FIFO).
     *
     * @param experience the experience transition to store.
     */
    void add(Experience experience);

    /**
     * Samples a mini-batch of experiences at random.
     *
     * @param batchSize the number of experiences to sample.
     * @return a TrainingBatch containing the sampled transitions.
     */
    TrainingBatch sample(int batchSize);

    /**
     * Returns the current number of experiences stored in the buffer.
     *
     * @return the number of transitions in the buffer.
     */
    int size();

    /**
     * Checks if the buffer contains enough transitions to sample a batch of the given size.
     *
     * @param batchSize the size of the training batch.
     * @return true if the buffer size is greater than or equal to batchSize, false otherwise.
     */
    boolean isReady(int batchSize);
}
