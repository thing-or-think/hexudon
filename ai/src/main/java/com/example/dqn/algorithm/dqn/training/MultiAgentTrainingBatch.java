package com.example.dqn.algorithm.dqn.training;

import com.example.dqn.core.experience.AgentExperience;
import com.example.dqn.core.state.StateEncoder;
import com.example.dqn.core.action.ActionSpace;
import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;
import java.util.List;

/**
 * Helper class responsible for converting a collection of high-level domain
 * AgentExperience objects into a numeric TrainingBatch suitable for neural network training.
 */
public final class MultiAgentTrainingBatch {

    private MultiAgentTrainingBatch() {
        // Prevent instantiation
    }

    /**
     * Converts a list of AgentExperience objects into a numeric TrainingBatch.
     *
     * @param <S> the specific State type.
     * @param <A> the specific Action type.
     * @param experiences the raw experiences list.
     * @param stateEncoder the encoder to translate State to float[].
     * @param actionSpace the action space to find action indices.
     * @return a compiled numeric TrainingBatch.
     */
    @SuppressWarnings("unchecked")
    public static <S extends State, A extends Action> TrainingBatch from(
            List<AgentExperience> experiences,
            StateEncoder<S> stateEncoder,
            ActionSpace<A> actionSpace
    ) {
        if (experiences == null || stateEncoder == null || actionSpace == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        int batchSize = experiences.size();
        float[][] states = new float[batchSize][];
        int[] actions = new int[batchSize];
        float[] rewards = new float[batchSize];
        float[][] nextStates = new float[batchSize][];
        boolean[] dones = new boolean[batchSize];

        for (int i = 0; i < batchSize; i++) {
            AgentExperience exp = experiences.get(i);
            states[i] = stateEncoder.encode((S) exp.state());
            actions[i] = actionSpace.indexOf((A) exp.action());
            rewards[i] = (float) exp.reward();
            nextStates[i] = stateEncoder.encode((S) exp.nextState());
            dones[i] = exp.done();
        }

        return new TrainingBatch(states, actions, rewards, nextStates, dones);
    }
}
