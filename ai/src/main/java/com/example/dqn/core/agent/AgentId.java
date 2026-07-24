package com.example.dqn.core.agent;

/**
 * Immutable identifier for an agent, encapsulating its unique string representation and type.
 */
public record AgentId(String value, AgentType type) {
    @Override
    public String toString() {
        return value;
    }
}
