package com.example.dqn.algorithm.dqn.agent;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.ActionSelector;
import com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class managing action selectors (policies) for each agent type in the system.
 */
public class AgentPolicy {

    private final Map<AgentType, ActionSelector> policies = new ConcurrentHashMap<>();

    /**
     * Registers an ActionSelector policy for a specific AgentType.
     */
    public void register(AgentType type, ActionSelector selector) {
        if (type == null || selector == null) {
            throw new IllegalArgumentException("Type and ActionSelector cannot be null");
        }
        policies.put(type, selector);
    }

    /**
     * Retrieves the ActionSelector policy for the given AgentType.
     */
    public ActionSelector policyFor(AgentType type) {
        return policies.get(type);
    }

    /**
     * Decays the exploration parameters (epsilon) across all registered policies.
     */
    public void decayEpsilons() {
        for (ActionSelector selector : policies.values()) {
            if (selector instanceof EpsilonGreedyPolicy) {
                ((EpsilonGreedyPolicy) selector).decayEpsilon();
            }
        }
    }

    /**
     * Resets the schedule step counters across all registered epsilon policies.
     */
    public void resetSteps() {
        for (ActionSelector selector : policies.values()) {
            if (selector instanceof EpsilonGreedyPolicy) {
                ((EpsilonGreedyPolicy) selector).setStep(0);
            }
        }
    }
}
