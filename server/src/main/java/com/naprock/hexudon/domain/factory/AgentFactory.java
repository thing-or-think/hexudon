package com.naprock.hexudon.domain.factory;

import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

public class AgentFactory {

    private AgentFactory() {
    }

    public static Agent create(int type, Coordinate startPosition) {
        return switch (type) {
            case 0 -> new PatrolAgent(startPosition);
            case 1 -> new RefuelAgent(startPosition);
            default -> throw new IllegalArgumentException("Unknown agent type: " + type);
        };
    }
}