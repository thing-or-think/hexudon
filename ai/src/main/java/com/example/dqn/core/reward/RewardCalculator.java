package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;

/**
 * Domain Service responsible for calculating cooperative multi-agent reward feedback
 * using dynamically loaded RewardProfiles.
 */
public class RewardCalculator {
    private final RewardProfileRegistry registry;
    private TrainingStatistics statistics;

    public RewardCalculator(RewardProfileRegistry registry) {
        this.registry = registry;
    }

    public void setStatistics(TrainingStatistics statistics) {
        this.statistics = statistics;
    }

    public TrainingStatistics getStatistics() {
        return statistics;
    }

    /**
     * Calculates the reward for an agent based on the provided context.
     * Also records statistics if a tracker is set.
     */
    public double calculate(RewardContext context) {
        RewardProfile profile = registry.getProfile(context.getAgentType());
        if (profile == null) {
            return 0.0;
        }

        double reward = 0.0;

        if (context.getAgentType() == AgentType.PATROL) {
            // 1. Invalid action penalty
            if (context.isOutOfBoundsOrNotWalkable()) {
                reward -= profile.getInvalidActionPenalty();
            } else {
                // travel steps penalty
                reward -= context.getTravelSteps() * profile.getIdlePenalty();
            }

            // 2. Udon collection reward
            int collected = context.getCollectedUdon();
            if (collected > 0) {
                reward += collected * profile.getUdonCollectedReward() * profile.getUdonCollectionMultiplier();
                reward += profile.getSuccessfulCollectionBonus();
            }

            // 3. Idle penalty (no progress)
            if (context.isSpotAlreadyCollected() && collected == 0) {
                reward -= profile.getIdlePenalty();
            }

            // 4. Out of fuel penalty
            if (context.isOutOfFuel()) {
                reward -= profile.getInvalidActionPenalty() * 2.0; // severe penalty
            }

            // 5. Cooperation/refueled reward
            if (context.isRefueled()) {
                reward += profile.getCooperationReward();
            }

            // 6. Fuel consumed penalty
            reward -= context.getFuelConsumed() * profile.getFuelConsumedPenalty();

        } else if (context.getAgentType() == AgentType.REFUEL) {
            // 1. Invalid action penalty
            if (context.isOutOfBoundsOrNotWalkable()) {
                reward -= profile.getInvalidActionPenalty();
            } else {
                reward -= context.getTravelSteps() * profile.getIdlePenalty();
            }

            // 2. Udon collection reward
            int collected = context.getCollectedUdon();
            if (collected > 0) {
                reward += collected * profile.getUdonCollectedReward() * profile.getUdonCollectionMultiplier();
            }

            if (context.isSpotAlreadyCollected() && collected == 0) {
                reward -= profile.getIdlePenalty();
            }

            // 3. Refuel success reward
            if (context.isRefueledPatrol()) {
                reward += profile.getRefuelSuccessReward();
            }

            // 4. Supported patrol reward / cooperation
            if (context.getPatrolAgentsSupported() > 0) {
                reward += context.getPatrolAgentsSupported() * profile.getSupportedPatrolReward();
            }

            // 5. Fuel delivered reward
            reward += context.getFuelDelivered() * profile.getFuelDeliveredReward();

            // 6. Neglecting patrol agents in danger penalty
            if (context.isAnyPatrolOutOfFuel()) {
                reward -= profile.getIdlePenalty();
            }
        }

        // Record statistics if statistics tracker is registered
        if (statistics != null) {
            statistics.record(context);
        }

        return reward;
    }

    /**
     * Calculates the shared team reward.
     */
    public double calculateTeamReward(int totalCollectedUdon, int totalOutOfFuelPatrols) {
        RewardProfile patrolProfile = registry.getProfile(AgentType.PATROL);
        double udonVal = patrolProfile != null ? patrolProfile.getUdonCollectedReward() : 1.0;
        double outOfFuelPen = patrolProfile != null ? patrolProfile.getInvalidActionPenalty() : 2.0;
        return totalCollectedUdon * udonVal - outOfFuelPen * totalOutOfFuelPatrols;
    }
}
