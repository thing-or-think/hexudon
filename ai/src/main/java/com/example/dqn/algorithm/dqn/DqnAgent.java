package com.example.dqn.algorithm.dqn;

import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.action.ActionSelector;
import com.example.dqn.core.action.ActionSpace;
import com.example.dqn.core.network.QNetwork;
import com.example.dqn.core.state.StateEncoder;
import com.example.dqn.core.epsilon.EpsilonSchedule;

/**
 * DQN Agent acting as a controller mediating between domain-specific components
 * and deep neural networks. Fully generic in terms of State (S) and Action (A).
 *
 * @param <S> the domain state type.
 * @param <A> the domain action type.
 */
public class DqnAgent<S extends State, A extends Action> implements AutoCloseable {

    private final QNetwork onlineNetwork;
    private final QNetwork targetNetwork;
    private final StateEncoder<S> stateEncoder;
    private final ActionSpace<A> actionSpace;
    private final ActionSelector actionSelector;
    private final EpsilonSchedule epsilonSchedule;
    private final DqnConfig config;

    /**
     * Constructs a DqnAgent.
     *
     * @param onlineNetwork the active Q-value network.
     * @param targetNetwork the target Q-value network.
     * @param stateEncoder adapter to convert domain state S to float vector.
     * @param actionSpace adapter to map discrete indices to domain actions A.
     * @param actionSelector the exploration/exploitation strategy.
     * @param epsilonSchedule schedule computing exploration rates.
     * @param config configuration holding dqn hyperparameters.
     */
    public DqnAgent(
            QNetwork onlineNetwork,
            QNetwork targetNetwork,
            StateEncoder<S> stateEncoder,
            ActionSpace<A> actionSpace,
            ActionSelector actionSelector,
            EpsilonSchedule epsilonSchedule,
            DqnConfig config
    ) {
        this.onlineNetwork = onlineNetwork;
        this.targetNetwork = targetNetwork;
        this.stateEncoder = stateEncoder;
        this.actionSpace = actionSpace;
        this.actionSelector = actionSelector;
        this.epsilonSchedule = epsilonSchedule;
        this.config = config;
    }

    /**
     * Encodes the domain state, predicts Q-values, and returns the selected action.
     *
     * @param state the current domain state.
     * @return the selected domain action.
     */
    public A selectAction(S state) {
        float[] stateVector = stateEncoder.encode(state);
        int actionIndex = actionSelector.selectAction(stateVector, onlineNetwork, actionSpace);
        return actionSpace.actionAt(actionIndex);
    }

    /**
     * Gets the online network.
     *
     * @return the online network.
     */
    public QNetwork getOnlineNetwork() {
        return onlineNetwork;
    }

    /**
     * Gets the target network.
     *
     * @return the target network.
     */
    public QNetwork getTargetNetwork() {
        return targetNetwork;
    }

    /**
     * Gets the state encoder.
     *
     * @return the state encoder.
     */
    public StateEncoder<S> getStateEncoder() {
        return stateEncoder;
    }

    /**
     * Gets the action space.
     *
     * @return the action space.
     */
    public ActionSpace<A> getActionSpace() {
        return actionSpace;
    }

    /**
     * Gets the action selector policy.
     *
     * @return the action selector.
     */
    public ActionSelector getActionSelector() {
        return actionSelector;
    }

    /**
     * Gets the epsilon schedule.
     *
     * @return the schedule.
     */
    public EpsilonSchedule getEpsilonSchedule() {
        return epsilonSchedule;
    }

    /**
     * Gets the configuration.
     *
     * @return the config.
     */
    public DqnConfig getConfig() {
        return config;
    }

    @Override
    public void close() {
        try {
            onlineNetwork.close();
        } catch (Exception e) {
            // Suppressed
        }
        try {
            targetNetwork.close();
        } catch (Exception e) {
            // Suppressed
        }
    }
}
