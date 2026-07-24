package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;

/**
 * Thread-safe tracker to accumulate training metrics across episodes for fitness evaluation.
 */
public class TrainingStatistics {
    // General
    private int totalUdonCollected = 0;
    private int totalFuelConsumed = 0;
    private int totalFuelDelivered = 0;
    private int totalRefuelSuccess = 0;
    private int totalSupportedPatrols = 0;
    private int totalInvalidActions = 0;
    private int completedEpisodes = 0;
    private int failedEpisodes = 0;
    private double totalEpisodeRewardSum = 0.0;
    private int totalEpisodeLengthSum = 0;

    // PATROL specific
    private int patrolUdonCollected = 0;
    private int patrolFuelConsumed = 0;
    private int patrolEpisodesCompleted = 0;
    private int patrolCollectionSuccessCount = 0;

    // REFUEL specific
    private int refuelFuelDelivered = 0;
    private int refuelSuccessCount = 0;
    private int refuelPatrolsSupported = 0;

    public synchronized void record(RewardContext context) {
        if (context.getAgentType() == AgentType.PATROL) {
            if (context.isOutOfBoundsOrNotWalkable()) {
                totalInvalidActions++;
            }
            if (context.getCollectedUdon() > 0) {
                totalUdonCollected += context.getCollectedUdon();
                patrolUdonCollected += context.getCollectedUdon();
                patrolCollectionSuccessCount++;
            }
            totalFuelConsumed += context.getFuelConsumed();
            patrolFuelConsumed += context.getFuelConsumed();
        } else if (context.getAgentType() == AgentType.REFUEL) {
            if (context.isOutOfBoundsOrNotWalkable()) {
                totalInvalidActions++;
            }
            if (context.getCollectedUdon() > 0) {
                totalUdonCollected += context.getCollectedUdon();
            }
            if (context.isRefueledPatrol()) {
                totalRefuelSuccess++;
                refuelSuccessCount++;
            }
            if (context.getPatrolAgentsSupported() > 0) {
                totalSupportedPatrols += context.getPatrolAgentsSupported();
                refuelPatrolsSupported += context.getPatrolAgentsSupported();
            }
            if (context.getFuelDelivered() > 0) {
                totalFuelDelivered += context.getFuelDelivered();
                refuelFuelDelivered += context.getFuelDelivered();
            }
        }
    }

    public synchronized void recordEpisodeEnd(boolean completed, int length, double reward) {
        if (completed) {
            completedEpisodes++;
            patrolEpisodesCompleted++;
        } else {
            failedEpisodes++;
        }
        totalEpisodeLengthSum += length;
        totalEpisodeRewardSum += reward;
    }

    public synchronized void reset() {
        totalUdonCollected = 0;
        totalFuelConsumed = 0;
        totalFuelDelivered = 0;
        totalRefuelSuccess = 0;
        totalSupportedPatrols = 0;
        totalInvalidActions = 0;
        completedEpisodes = 0;
        failedEpisodes = 0;
        totalEpisodeRewardSum = 0.0;
        totalEpisodeLengthSum = 0;
        patrolUdonCollected = 0;
        patrolFuelConsumed = 0;
        patrolEpisodesCompleted = 0;
        patrolCollectionSuccessCount = 0;
        refuelFuelDelivered = 0;
        refuelSuccessCount = 0;
        refuelPatrolsSupported = 0;
    }

    public int getTotalUdonCollected() {
        return totalUdonCollected;
    }

    public int getTotalFuelConsumed() {
        return totalFuelConsumed;
    }

    public int getTotalFuelDelivered() {
        return totalFuelDelivered;
    }

    public int getTotalRefuelSuccess() {
        return totalRefuelSuccess;
    }

    public int getTotalSupportedPatrols() {
        return totalSupportedPatrols;
    }

    public int getTotalInvalidActions() {
        return totalInvalidActions;
    }

    public int getCompletedEpisodes() {
        return completedEpisodes;
    }

    public int getFailedEpisodes() {
        return failedEpisodes;
    }

    public double getAverageEpisodeReward() {
        int totalEpisodes = completedEpisodes + failedEpisodes;
        return totalEpisodes > 0 ? (totalEpisodeRewardSum / totalEpisodes) : 0.0;
    }

    public double getAverageEpisodeLength() {
        int totalEpisodes = completedEpisodes + failedEpisodes;
        return totalEpisodes > 0 ? ((double) totalEpisodeLengthSum / totalEpisodes) : 0.0;
    }

    public int getPatrolUdonCollected() {
        return patrolUdonCollected;
    }

    public int getPatrolFuelConsumed() {
        return patrolFuelConsumed;
    }

    public int getPatrolEpisodesCompleted() {
        return patrolEpisodesCompleted;
    }

    public int getPatrolCollectionSuccessCount() {
        return patrolCollectionSuccessCount;
    }

    public double getPatrolCollectionEfficiency() {
        return patrolFuelConsumed > 0 ? ((double) patrolUdonCollected / patrolFuelConsumed) : 0.0;
    }

    public int getRefuelFuelDelivered() {
        return refuelFuelDelivered;
    }

    public int getRefuelSuccessCount() {
        return refuelSuccessCount;
    }

    public int getRefuelPatrolsSupported() {
        return refuelPatrolsSupported;
    }
}
