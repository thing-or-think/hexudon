package com.example.dqn.application.service;

import com.example.dqn.application.port.in.EvaluateAgentUseCase;
import com.example.dqn.core.environment.MultiAgentEnvironment;
import com.example.dqn.core.environment.MultiAgentEnvironment.MultiAgentStepResult;
import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.DqnAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrates multi-agent evaluation by running greedily in the environment.
 */
public class EvaluationService implements EvaluateAgentUseCase<State, Action> {

    private final MultiAgentEnvironment environment;
    private final AgentNetworkRegistry agentNetworkRegistry;

    /**
     * Constructs an EvaluationService.
     */
    public EvaluationService(MultiAgentEnvironment environment, AgentNetworkRegistry agentNetworkRegistry) {
        this.environment = environment;
        this.agentNetworkRegistry = agentNetworkRegistry;
    }

    @Override
    public double evaluate(int episodes) {
        double totalRewards = 0.0;
        System.out.println("Starting evaluation...");
        for (int ep = 1; ep <= episodes; ep++) {
            Map<AgentId, State> states = environment.reset();
            double rewardSum = 0.0;
            boolean done = false;
            while (!done && !environment.isDone()) {
                Map<AgentId, Action> actions = new HashMap<>();
                for (Map.Entry<AgentId, State> entry : states.entrySet()) {
                    AgentId agentId = entry.getKey();
                    State state = entry.getValue();
                    DqnAgent<?, ?> dqnAgent = agentNetworkRegistry.agentFor(agentId.type());
                    Action action = selectActionHelper(dqnAgent, state);
                    actions.put(agentId, action);
                }
                MultiAgentStepResult result = environment.step(actions);
                rewardSum += result.teamReward();
                states = result.nextStates();
                done = result.done();
            }
            totalRewards += rewardSum;
            System.out.printf("Evaluation Ep %d | Total Team Reward: %.2f%n", ep, rewardSum);
        }
        double avgReward = totalRewards / episodes;
        System.out.printf("Evaluation finished. Average Team Reward: %.2f%n", avgReward);
        return avgReward;
    }

    @SuppressWarnings("unchecked")
    private <S extends State, A extends Action> A selectActionHelper(DqnAgent<S, A> agent, State state) {
        return agent.selectAction((S) state);
    }
}
