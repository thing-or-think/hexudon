package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain container holding metadata and reward profiles for all AgentTypes.
 */
public class RewardProfileContainer {
    private int version;
    private int generation;
    private Map<AgentType, RewardProfile> profiles;

    public RewardProfileContainer() {
        // Default constructor for Jackson
    }

    public RewardProfileContainer(int version, int generation, Map<AgentType, RewardProfile> profiles) {
        this.version = version;
        this.generation = generation;
        this.profiles = profiles;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public Map<AgentType, RewardProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<AgentType, RewardProfile> profiles) {
        this.profiles = profiles;
    }

    /**
     * Factory method creating a default reward profile container.
     */
    public static RewardProfileContainer createDefault() {
        Map<AgentType, RewardProfile> profiles = new HashMap<>();

        RewardProfile patrol = new RewardProfile();
        patrol.setUdonCollectedReward(10.0);
        patrol.setUdonCollectionMultiplier(1.5);
        patrol.setFuelConsumedPenalty(0.5);
        patrol.setInvalidActionPenalty(2.0);
        patrol.setIdlePenalty(0.2);
        patrol.setSuccessfulCollectionBonus(5.0);
        patrol.setCooperationReward(3.0);
        profiles.put(AgentType.PATROL, patrol);

        RewardProfile refuel = new RewardProfile();
        refuel.setRefuelSuccessReward(5.0);
        refuel.setSupportedPatrolReward(8.0);
        refuel.setFuelDeliveredReward(1.0);
        refuel.setInvalidActionPenalty(2.0);
        refuel.setIdlePenalty(0.2);
        profiles.put(AgentType.REFUEL, refuel);

        return new RewardProfileContainer(1, 0, profiles);
    }
}
