package com.example.dqn.algorithm.dqn.transition;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;
import com.example.dqn.core.experience.AgentExperience;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.algorithm.dqn.action.AgentAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds individual AgentExperience transition tuples from step transition results.
 */
public class ExperienceBuilder {

    /**
     * Builds and returns a list of agent experiences.
     *
     * @param currentState initial multi-agent state.
     * @param actions actions selected.
     * @param transition simulated transition results.
     * @return list of agent experiences.
     */
    public List<AgentExperience> build(
            MultiAgentState currentState,
            List<AgentAction> actions,
            TransitionResult transition
    ) {
        List<AgentExperience> experiences = new ArrayList<>();
        if (currentState == null || currentState.agentStates() == null || transition == null) {
            return experiences;
        }

        currentState.agentStates().forEach((agentId, agentState) -> {
            AgentType type = agentId.type();

            Action action = actions.stream()
                    .filter(a -> a.agentId().equals(agentId))
                    .map(AgentAction::action)
                    .findFirst()
                    .orElse(null);

            State nextState = transition.nextState().agentStates().get(agentId);
            double reward = transition.individualRewards().getOrDefault(agentId, 0.0);
            boolean done = transition.done();

            if (action != null && nextState != null) {
                experiences.add(new AgentExperience(
                        agentId,
                        type,
                        agentState,
                        action,
                        reward,
                        nextState,
                        done
                ));
            }
        });

        return experiences;
    }
}
