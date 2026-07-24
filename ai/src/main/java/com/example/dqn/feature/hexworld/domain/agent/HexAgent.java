package com.example.dqn.feature.hexworld.domain.agent;

import com.example.dqn.core.agent.Agent;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.feature.hexworld.domain.HexPosition;

/**
 * Abstract base class for reinforcement learning agents acting within the HexWorld map.
 */
public abstract class HexAgent implements Agent {

    protected final AgentId id;
    protected HexPosition position;
    protected int collectedUdon;

    protected HexAgent(AgentId id, HexPosition position) {
        if (id == null || position == null) {
            throw new IllegalArgumentException("Id and Position cannot be null");
        }
        this.id = id;
        this.position = position;
        this.collectedUdon = 0;
    }

    @Override
    public AgentId id() {
        return id;
    }

    public HexPosition position() {
        return position;
    }

    public void setPosition(HexPosition position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = position;
    }

    public int collectedUdon() {
        return collectedUdon;
    }

    public void setCollectedUdon(int collectedUdon) {
        if (collectedUdon < 0) {
            throw new IllegalArgumentException("Cannot set negative Udon");
        }
        this.collectedUdon = collectedUdon;
    }

    public void addUdon(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative Udon");
        }
        this.collectedUdon += amount;
    }

    /**
     * Resets the agent's position and metrics to starting conditions.
     */
    public void reset(HexPosition startPos) {
        if (startPos == null) {
            throw new IllegalArgumentException("Start position cannot be null");
        }
        this.position = startPos;
        this.collectedUdon = 0;
    }
}
