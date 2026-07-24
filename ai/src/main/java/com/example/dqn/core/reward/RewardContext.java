package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;

/**
 * Context containing all state parameters and event occurrences in a step
 * required to calculate the reward for a specific agent.
 */
public class RewardContext {
    private final AgentType agentType;
    private final boolean outOfBoundsOrNotWalkable;
    private final int travelSteps;
    private final int collectedUdon;
    private final boolean spotAlreadyCollected;
    private final boolean outOfFuel;
    private final boolean refueled;
    private final int fuelConsumed;

    // Refuel Agent specific or interaction parameters
    private final boolean refueledPatrol;
    private final boolean anyPatrolOutOfFuel;
    private final int fuelDelivered;
    private final int patrolAgentsSupported;

    public RewardContext(
            AgentType agentType,
            boolean outOfBoundsOrNotWalkable,
            int travelSteps,
            int collectedUdon,
            boolean spotAlreadyCollected,
            boolean outOfFuel,
            boolean refueled,
            int fuelConsumed,
            boolean refueledPatrol,
            boolean anyPatrolOutOfFuel,
            int fuelDelivered,
            int patrolAgentsSupported
    ) {
        this.agentType = agentType;
        this.outOfBoundsOrNotWalkable = outOfBoundsOrNotWalkable;
        this.travelSteps = travelSteps;
        this.collectedUdon = collectedUdon;
        this.spotAlreadyCollected = spotAlreadyCollected;
        this.outOfFuel = outOfFuel;
        this.refueled = refueled;
        this.fuelConsumed = fuelConsumed;
        this.refueledPatrol = refueledPatrol;
        this.anyPatrolOutOfFuel = anyPatrolOutOfFuel;
        this.fuelDelivered = fuelDelivered;
        this.patrolAgentsSupported = patrolAgentsSupported;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public boolean isOutOfBoundsOrNotWalkable() {
        return outOfBoundsOrNotWalkable;
    }

    public int getTravelSteps() {
        return travelSteps;
    }

    public int getCollectedUdon() {
        return collectedUdon;
    }

    public boolean isSpotAlreadyCollected() {
        return spotAlreadyCollected;
    }

    public boolean isOutOfFuel() {
        return outOfFuel;
    }

    public boolean isRefueled() {
        return refueled;
    }

    public int getFuelConsumed() {
        return fuelConsumed;
    }

    public boolean isRefueledPatrol() {
        return refueledPatrol;
    }

    public boolean isAnyPatrolOutOfFuel() {
        return anyPatrolOutOfFuel;
    }

    public int getFuelDelivered() {
        return fuelDelivered;
    }

    public int getPatrolAgentsSupported() {
        return patrolAgentsSupported;
    }
}
