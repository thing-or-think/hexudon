package com.example.dqn.core.agent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry mapping agent identifiers to active Agent instances.
 */
public class AgentRegistry {
    
    private final Map<AgentId, Agent> agents = new ConcurrentHashMap<>();

    /**
     * Registers a new agent.
     */
    public void register(Agent agent) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent cannot be null");
        }
        agents.put(agent.id(), agent);
    }

    /**
     * Resolves an agent by its ID.
     */
    public Optional<Agent> get(AgentId id) {
        return Optional.ofNullable(agents.get(id));
    }

    /**
     * Gets all registered agents.
     */
    public Collection<Agent> allAgents() {
        return agents.values();
    }

    /**
     * Clears all registrations.
     */
    public void clear() {
        agents.clear();
    }
}
