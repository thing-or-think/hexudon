package com.example.dqn.application.port.out;

import com.example.dqn.core.network.QNetwork;

/**
 * Output port (SPI) for model saving and loading operations.
 */
public interface ModelStore {

    /**
     * Saves the network parameters.
     *
     * @param network the Q-Network to save.
     * @param directory target directory.
     * @param modelName name prefix of model.
     */
    void save(QNetwork network, String directory, String modelName);

    /**
     * Loads the network parameters.
     *
     * @param network the Q-Network to load parameters into.
     * @param directory source directory.
     * @param modelName name prefix of model.
     */
    void load(QNetwork network, String directory, String modelName);
}
