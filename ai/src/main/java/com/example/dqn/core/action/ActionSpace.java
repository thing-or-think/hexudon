package com.example.dqn.core.action;

/**
 * Encapsulates the discrete actions available for a specific reinforcement learning problem.
 * Maps high-level domain actions to low-level indices used by the Neural Network, and vice-versa.
 *
 * @param <A> the domain-specific Action type.
 */
public interface ActionSpace<A extends Action> {

    /**
     * The total number of discrete actions in the space.
     *
     * @return count of available actions.
     */
    int size();

    /**
     * Resolves an action index (as returned by the policy or neural network) to its corresponding domain action.
     *
     * @param index the index of the action.
     * @return the domain action A.
     */
    A actionAt(int index);

    /**
     * Resolves a domain action to its index representation in the action space.
     *
     * @param action the domain action A.
     * @return the corresponding index.
     */
    int indexOf(A action);
}
