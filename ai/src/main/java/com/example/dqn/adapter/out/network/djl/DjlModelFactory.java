package com.example.dqn.adapter.out.network.djl;

import ai.djl.nn.Activation;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;

/**
 * Factory class to create standard Multi-Layer Perceptron (MLP) blocks for DQN using DJL.
 */
public final class DjlModelFactory {

    private DjlModelFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a feedforward sequential neural network block with dense layers and ReLU activation functions.
     *
     * @param stateDimension the size of the input state vector.
     * @param actionSpaceSize the size of the output Q-value vector (number of discrete actions).
     * @param hiddenLayers an array specifying the size of each hidden layer.
     * @return a SequentialBlock representing the MLP architecture.
     */
    public static SequentialBlock createMlp(int stateDimension, int actionSpaceSize, int[] hiddenLayers) {
        SequentialBlock block = new SequentialBlock();
        
        // Input validation
        if (stateDimension <= 0) {
            throw new IllegalArgumentException("State dimension must be positive");
        }
        if (actionSpaceSize <= 0) {
            throw new IllegalArgumentException("Action space size must be positive");
        }
        if (hiddenLayers == null || hiddenLayers.length == 0) {
            throw new IllegalArgumentException("Must specify at least one hidden layer");
        }

        // Add hidden layers
        for (int units : hiddenLayers) {
            if (units <= 0) {
                throw new IllegalArgumentException("Hidden layer size must be positive");
            }
            block.add(Linear.builder().setUnits(units).build());
            block.add(Activation.reluBlock());
        }

        // Add output layer (no activation function since outputs are raw real-valued Q-values)
        block.add(Linear.builder().setUnits(actionSpaceSize).build());

        return block;
    }
}
