package com.example.dqn.core.environment;

import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;

/**
 * Represents a Reinforcement Learning environment.
 *
 * @param <S> The type representing the state of the environment.
 * @param <A> The type representing the actions available in the environment.
 */
public interface Environment<S extends State, A extends Action> {

    /**
     * Resets the environment to its initial state.
     *
     * @return the initial state S.
     */
    S reset();

    /**
     * Returns the current state of the environment.
     *
     * @return the current state S.
     */
    S currentState();

    /**
     * Executes the given action in the environment, transitions to the next state,
     * and returns the result.
     *
     * @param action the action to execute.
     * @return the step result containing the next state, reward, and done flag.
     */
    StepResult<S> step(A action);

    /**
     * Checks if the current episode has terminated.
     *
     * @return true if the current episode is done, false otherwise.
     */
    boolean isDone();
}
