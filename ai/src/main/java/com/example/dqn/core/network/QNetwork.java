package com.example.dqn.core.network;

import com.example.dqn.algorithm.dqn.training.TrainingBatch;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface isolating the core DQN training logic from the details of the deep learning library.
 * Represents an action-value function approximator Q(s, a).
 */
public interface QNetwork extends AutoCloseable {

    /**
     * Predicts Q-values for a single state.
     *
     * @param state the encoded state vector.
     * @return an array of Q-values, one for each discrete action.
     */
    float[] predict(float[] state);

    /**
     * Predicts Q-values for a batch of states.
     *
     * @param states a batch of encoded state vectors [batchSize, stateDimension].
     * @return a 2D array [batchSize, actionSpaceSize] containing predicted Q-values.
     */
    float[][] predictBatch(float[][] states);

    /**
     * Executes a single gradient descent training step on a batch of experiences
     * using the computed Q-targets as training labels.
     *
     * @param batch the training batch.
     * @param targets the target Q-value labels of shape [batchSize, actionSpaceSize].
     * @return the scalar loss value of the training step.
     */
    float train(TrainingBatch batch, float[][] targets);

    /**
     * Synchronizes weights by copying parameters from a source network (e.g., online network)
     * to this target network.
     *
     * @param source the source QNetwork to copy weights from.
     */
    void copyParametersFrom(QNetwork source);

    /**
     * Saves the network model parameters to the specified directory.
     *
     * @param modelPath directory path to save.
     * @param modelName name of the model.
     * @throws IOException if saving fails due to I/O error.
     */
    void save(Path modelPath, String modelName) throws IOException;

    /**
     * Loads the network model parameters from the specified directory.
     *
     * @param modelPath directory path to load.
     * @param modelName name of the model.
     * @throws Exception if loading fails.
     */
    void load(Path modelPath, String modelName) throws Exception;

    /**
     * Closes the network, releasing native resources and memory managers.
     */
    @Override
    void close();
}
