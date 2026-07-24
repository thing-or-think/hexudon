package com.example.dqn.algorithm.dqn.transition;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.action.Action;
import com.example.dqn.feature.hexworld.HexWorld;
import com.example.dqn.feature.hexworld.domain.UdonCollectionState;
import com.example.dqn.feature.hexworld.domain.agent.PatrolAgent;
import com.example.dqn.feature.hexworld.domain.agent.RefuelAgent;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.feature.hexworld.domain.state.PatrolState;
import com.example.dqn.feature.hexworld.domain.state.RefuelState;
import com.example.dqn.core.environment.MultiAgentEnvironment.MultiAgentStepResult;
import com.example.dqn.algorithm.dqn.action.AgentAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transition simulator that uses the local environment instance to predict state steps.
 */
public class LocalTransitionSimulator {
    private final HexWorld env;

    public LocalTransitionSimulator(HexWorld env) {
        if (env == null) {
            throw new IllegalArgumentException("HexWorld environment cannot be null");
        }
        this.env = env;
    }

    /**
     * Simulates the transition from currentState using the given actions.
     *
     * @param currentState current multi-agent state.
     * @param actions actions selected.
     * @return transition results.
     */
    public TransitionResult simulate(MultiAgentState currentState, List<AgentAction> actions) {
        // 1. Synchronize the local environment state with the given currentState
        synchronizeState(currentState);

        // 2. Map List<AgentAction> to Map<AgentId, Action>
        Map<AgentId, Action> actionsMap = new HashMap<>();
        for (AgentAction act : actions) {
            actionsMap.put(act.agentId(), act.action());
        }

        // 3. Run a step on the local environment
        MultiAgentStepResult stepResult = env.step(actionsMap);

        // 4. Construct next MultiAgentState
        MultiAgentState nextState = new MultiAgentState(
                stepResult.nextStates(),
                env.getCollectedState().collectedPositions()
        );

        return new TransitionResult(
                nextState,
                stepResult.individualRewards(),
                stepResult.teamReward(),
                stepResult.done()
        );
    }

    private void synchronizeState(MultiAgentState multiState) {
        if (multiState == null) return;

        // Synchronize remaining steps
        multiState.agentStates().values().stream()
                .filter(s -> s instanceof PatrolState)
                .map(s -> ((PatrolState) s).remainingSteps())
                .findFirst()
                .ifPresent(env::setRemainingSteps);

        // Synchronize Udon collection state
        if (multiState.collectedUdonSpots() != null) {
            env.setCollectedState(new UdonCollectionState(multiState.collectedUdonSpots()));
        }

        // Synchronize agents
        multiState.agentStates().forEach((agentId, state) -> {
            if (state instanceof PatrolState patrolState) {
                PatrolAgent agent = findPatrolAgent(agentId);
                if (agent != null) {
                    agent.setPosition(patrolState.selfPosition());
                    agent.setFuel(patrolState.fuel());
                    agent.setCollectedUdon(patrolState.collectedUdon());
                }
            } else if (state instanceof RefuelState refuelState) {
                RefuelAgent agent = findRefuelAgent(agentId);
                if (agent != null) {
                    agent.setPosition(refuelState.selfPosition());
                    agent.setCollectedUdon(refuelState.collectedUdon());
                }
            }
        });
    }

    private PatrolAgent findPatrolAgent(AgentId id) {
        return env.getPatrolAgents().stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private RefuelAgent findRefuelAgent(AgentId id) {
        return env.getRefuelAgents().stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
