package com.example.dqn.algorithm.dqn.transition;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.core.agent.AgentId;
import java.util.Map;

/**
 * Immutable record representing the outcomes of a simulated state transition.
 *
 * @param nextState predicted transitioned multi-agent state.
 * @param individualRewards immediate individual reward mapping.
 * @param teamReward immediate team reward.
 * @param done termination status of the transition.
 */
public record TransitionResult(
    MultiAgentState nextState,
    Map<AgentId, double[]> individualRewardsArray, // Jackson compatible fallback
    Map<AgentId, Double> individualRewards,
    double teamReward,
    boolean done
) {
    public TransitionResult(
            MultiAgentState nextState,
            Map<AgentId, Double> individualRewards,
            double teamReward,
            boolean done
    ) {
        this(nextState, null, individualRewards, teamReward, done);
    }
}
