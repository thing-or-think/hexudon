package com.example.dqn.core.state;

/**
 * Domain interface for holding and synchronizing state authoritative resources.
 *
 * @param <S> state representation type.
 */
public interface StateHolder<S extends State> {
    /**
     * Retrieves the current state representation.
     */
    S currentState();

    /**
     * Updates the held state.
     *
     * @param state new state.
     */
    void update(S state);
}
