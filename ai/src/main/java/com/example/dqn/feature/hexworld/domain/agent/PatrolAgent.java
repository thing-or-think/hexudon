package com.example.dqn.feature.hexworld.domain.agent;

import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.HexPosition;

/**
 * Concrete agent representing a Patrol Agent whose job is to explore the world
 * and gather Udon, consuming fuel while doing so, and requiring refueling support.
 */
public class PatrolAgent extends HexAgent {

    private int fuel;
    private final int maxFuel;

    public PatrolAgent(AgentId id, HexPosition position, int maxFuel) {
        super(id, position);
        if (maxFuel <= 0) {
            throw new IllegalArgumentException("Max fuel must be positive");
        }
        this.maxFuel = maxFuel;
        this.fuel = maxFuel;
    }

    @Override
    public AgentType type() {
        return AgentType.PATROL;
    }

    @Override
    public State state() {
        // State is constructed via Environment and StateEncoders.
        return null;
    }

    public int fuel() {
        return fuel;
    }

    public int maxFuel() {
        return maxFuel;
    }

    public void setFuel(int fuel) {
        this.fuel = Math.max(0, Math.min(maxFuel, fuel));
    }

    public void consumeFuel(int amount) {
        setFuel(this.fuel - amount);
    }

    public void refuel(int amount) {
        setFuel(this.fuel + amount);
    }

    public boolean isOutOfFuel() {
        return fuel <= 0;
    }

    @Override
    public void reset(HexPosition startPos) {
        super.reset(startPos);
        this.fuel = maxFuel;
    }
}
