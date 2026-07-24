package com.example.dqn.application.port.out;

import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import com.example.dqn.core.epsilon.EpsilonProfile;
import com.example.dqn.core.agent.AgentType;

/**
 * Output port for saving/loading EpsilonProfile configurations.
 */
public interface EpsilonProfileStore {
    /**
     * Loads the full profile container.
     */
    EpsilonProfileContainer load();

    /**
     * Loads the profile for a specific agent type.
     */
    EpsilonProfile load(AgentType agentType);

    /**
     * Loads the profile for a specific agent id.
     */
    EpsilonProfile load(String agentId);

    /**
     * Persists the profile container.
     */
    void save(EpsilonProfileContainer container);
}
