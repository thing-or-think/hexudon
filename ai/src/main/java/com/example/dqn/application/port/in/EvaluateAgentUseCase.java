package com.example.dqn.application.port.in;

import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;

/**
 * Input port for evaluating a reinforcement learning agent.
 *
 * @param <S> domain state type.
 * @param <A> domain action type.
 */
public interface EvaluateAgentUseCase<S extends State, A extends Action> {

    /**
     * Executes the evaluation phase for a specified number of episodes.
     *
     * @param episodes the number of episodes to evaluate.
     * @return the average reward per episode.
     */
    double evaluate(int episodes);
}
