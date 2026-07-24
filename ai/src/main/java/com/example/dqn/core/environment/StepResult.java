package com.example.dqn.core.environment;

import com.example.dqn.core.state.State;

/**
 * Represents the outcome of taking a single step/action in the environment.
 *
 * @param <S> the state type.
 * @param nextState the state transitioned to after the step.
 * @param reward the immediate scalar reward received from the step.
 * @param done whether the environment has reached a terminal state.
 */
public record StepResult<S extends State>(
    S nextState,
    double reward,
    boolean done
) {}
