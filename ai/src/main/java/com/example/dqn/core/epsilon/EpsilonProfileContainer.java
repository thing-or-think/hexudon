package com.example.dqn.core.epsilon;

import com.example.dqn.core.agent.AgentType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain container holding metadata and exploration profiles for all AgentTypes.
 */
public class EpsilonProfileContainer {
    private int version = 1;
    private int generation = 0;
    private Map<AgentType, EpsilonProfile> profiles;

    // Metadata fields
    private double fitness;
    private int totalUdonCollectedCount;
    private double totalUdonCollectedValue;
    private double episodeReward;
    private String createdAt;

    public EpsilonProfileContainer() {
        // Default constructor for Jackson
    }

    public EpsilonProfileContainer(int version, int generation, Map<AgentType, EpsilonProfile> profiles) {
        this.version = version;
        this.generation = generation;
        this.profiles = profiles;
        this.createdAt = Instant.now().toString();
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

    public Map<AgentType, EpsilonProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<AgentType, EpsilonProfile> profiles) {
        this.profiles = profiles;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getTotalUdonCollectedCount() {
        return totalUdonCollectedCount;
    }

    public void setTotalUdonCollectedCount(int totalUdonCollectedCount) {
        this.totalUdonCollectedCount = totalUdonCollectedCount;
    }

    public double getTotalUdonCollectedValue() {
        return totalUdonCollectedValue;
    }

    public void setTotalUdonCollectedValue(double totalUdonCollectedValue) {
        this.totalUdonCollectedValue = totalUdonCollectedValue;
    }

    public double getEpisodeReward() {
        return episodeReward;
    }

    public void setEpisodeReward(double episodeReward) {
        this.episodeReward = episodeReward;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Factory method creating a default epsilon profile container.
     */
    public static EpsilonProfileContainer createDefault() {
        Map<AgentType, EpsilonProfile> profiles = new HashMap<>();

        EpsilonProfile patrol = new EpsilonProfile();
        patrol.setInitialEpsilon(1.0);
        patrol.setMinimumEpsilon(0.05);
        patrol.setDecayRate(0.95);
        patrol.setDecayStrategy("EXPONENTIAL");
        patrol.setExplorationDuration(1000);
        profiles.put(AgentType.PATROL, patrol);

        EpsilonProfile refuel = new EpsilonProfile();
        refuel.setInitialEpsilon(1.0);
        refuel.setMinimumEpsilon(0.05);
        refuel.setDecayRate(0.95);
        refuel.setDecayStrategy("EXPONENTIAL");
        refuel.setExplorationDuration(1000);
        profiles.put(AgentType.REFUEL, refuel);

        return new EpsilonProfileContainer(1, 0, profiles);
    }
}
