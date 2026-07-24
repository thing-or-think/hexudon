package com.example.dqn.algorithm.dqn.evolution;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.reward.RewardProfile;
import com.example.dqn.core.reward.RewardProfileContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Mutation strategy executing random parameter shifts within clamped boundaries.
 */
public class RewardMutationStrategy {
    private final Random random = new Random();

    public RewardProfileContainer mutate(RewardProfileContainer current, RewardEvolutionConfig config) {
        Map<AgentType, RewardProfile> mutatedProfiles = new HashMap<>();

        for (Map.Entry<AgentType, RewardProfile> entry : current.getProfiles().entrySet()) {
            AgentType type = entry.getKey();
            RewardProfile originalProfile = entry.getValue();
            RewardProfile mutatedProfile = mutateProfile(originalProfile, type, config);
            mutatedProfiles.put(type, mutatedProfile);
        }

        // Return container with same version but new profiles.
        // Generation increment will be handled by the engine after validation.
        return new RewardProfileContainer(current.getVersion(), current.getGeneration(), mutatedProfiles);
    }

    private RewardProfile mutateProfile(RewardProfile original, AgentType type, RewardEvolutionConfig config) {
        RewardProfile mutated = new RewardProfile();

        if (type == AgentType.PATROL) {
            mutated.setUdonCollectedReward(mutateValue(original.getUdonCollectedReward(), config.getMutationRate(), config.getMutationMagnitude(), config.minUdonCollectedReward(), config.maxUdonCollectedReward()));
            mutated.setUdonCollectionMultiplier(mutateValue(original.getUdonCollectionMultiplier(), config.getMutationRate(), config.getMutationMagnitude(), config.minUdonCollectionMultiplier(), config.maxUdonCollectionMultiplier()));
            mutated.setFuelConsumedPenalty(mutateValue(original.getFuelConsumedPenalty(), config.getMutationRate(), config.getMutationMagnitude(), config.minFuelConsumedPenalty(), config.maxFuelConsumedPenalty()));
            mutated.setInvalidActionPenalty(mutateValue(original.getInvalidActionPenalty(), config.getMutationRate(), config.getMutationMagnitude(), config.minInvalidActionPenalty(), config.maxInvalidActionPenalty()));
            mutated.setIdlePenalty(mutateValue(original.getIdlePenalty(), config.getMutationRate(), config.getMutationMagnitude(), config.minIdlePenalty(), config.maxIdlePenalty()));
            mutated.setSuccessfulCollectionBonus(mutateValue(original.getSuccessfulCollectionBonus(), config.getMutationRate(), config.getMutationMagnitude(), config.minSuccessfulCollectionBonus(), config.maxSuccessfulCollectionBonus()));
            mutated.setCooperationReward(mutateValue(original.getCooperationReward(), config.getMutationRate(), config.getMutationMagnitude(), config.minCooperationReward(), config.maxCooperationReward()));
        } else if (type == AgentType.REFUEL) {
            mutated.setRefuelSuccessReward(mutateValue(original.getRefuelSuccessReward(), config.getMutationRate(), config.getMutationMagnitude(), config.minRefuelSuccessReward(), config.maxRefuelSuccessReward()));
            mutated.setSupportedPatrolReward(mutateValue(original.getSupportedPatrolReward(), config.getMutationRate(), config.getMutationMagnitude(), config.minSupportedPatrolReward(), config.maxSupportedPatrolReward()));
            mutated.setFuelDeliveredReward(mutateValue(original.getFuelDeliveredReward(), config.getMutationRate(), config.getMutationMagnitude(), config.minFuelDeliveredReward(), config.maxFuelDeliveredReward()));
            mutated.setInvalidActionPenalty(mutateValue(original.getInvalidActionPenalty(), config.getMutationRate(), config.getMutationMagnitude(), config.minInvalidActionPenalty(), config.maxInvalidActionPenalty()));
            mutated.setIdlePenalty(mutateValue(original.getIdlePenalty(), config.getMutationRate(), config.getMutationMagnitude(), config.minIdlePenalty(), config.maxIdlePenalty()));
        }

        return mutated;
    }

    private double mutateValue(double originalValue, double rate, double magnitude, double min, double max) {
        if (random.nextDouble() < rate) {
            double change = (random.nextDouble() * 2.0 - 1.0) * magnitude;
            double newValue = originalValue + change;
            return clamp(newValue, min, max);
        }
        return originalValue;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
