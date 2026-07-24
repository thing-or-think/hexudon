package com.example.dqn.core.action;

import com.example.dqn.core.network.QNetwork;

/**
 * Interface responsible for choosing an action index given the current state vector
 * and a QNetwork representing the action-value values.
 */
public interface ActionSelector {

    /**
     * Chooses a discrete action index.
     *
     * @param state the encoded state vector.
     * @param network the Q-Network used to predict Q-values.
     * @param actionSpace the action space of the environment.
     * @return the selected action index.
     */
    int selectAction(float[] state, QNetwork network, ActionSpace<?> actionSpace);
}
