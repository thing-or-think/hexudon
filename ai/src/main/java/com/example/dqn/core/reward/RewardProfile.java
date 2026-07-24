package com.example.dqn.core.reward;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Domain model representing the reward configurations for a single AgentType.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RewardProfile {
    // Patrol parameters
    private double udonCollectedReward;
    private double udonCollectionMultiplier;
    private double fuelConsumedPenalty;
    private double invalidActionPenalty;
    private double idlePenalty;
    private double successfulCollectionBonus;
    private double cooperationReward;

    // Refuel parameters
    private double refuelSuccessReward;
    private double supportedPatrolReward;
    private double fuelDeliveredReward;

    public RewardProfile() {
        // Default constructor for Jackson
    }

    public double getUdonCollectedReward() {
        return udonCollectedReward;
    }

    public void setUdonCollectedReward(double udonCollectedReward) {
        this.udonCollectedReward = udonCollectedReward;
    }

    public double getUdonCollectionMultiplier() {
        return udonCollectionMultiplier;
    }

    public void setUdonCollectionMultiplier(double udonCollectionMultiplier) {
        this.udonCollectionMultiplier = udonCollectionMultiplier;
    }

    public double getFuelConsumedPenalty() {
        return fuelConsumedPenalty;
    }

    public void setFuelConsumedPenalty(double fuelConsumedPenalty) {
        this.fuelConsumedPenalty = fuelConsumedPenalty;
    }

    public double getInvalidActionPenalty() {
        return invalidActionPenalty;
    }

    public void setInvalidActionPenalty(double invalidActionPenalty) {
        this.invalidActionPenalty = invalidActionPenalty;
    }

    public double getIdlePenalty() {
        return idlePenalty;
    }

    public void setIdlePenalty(double idlePenalty) {
        this.idlePenalty = idlePenalty;
    }

    public double getSuccessfulCollectionBonus() {
        return successfulCollectionBonus;
    }

    public void setSuccessfulCollectionBonus(double successfulCollectionBonus) {
        this.successfulCollectionBonus = successfulCollectionBonus;
    }

    public double getCooperationReward() {
        return cooperationReward;
    }

    public void setCooperationReward(double cooperationReward) {
        this.cooperationReward = cooperationReward;
    }

    public double getRefuelSuccessReward() {
        return refuelSuccessReward;
    }

    public void setRefuelSuccessReward(double refuelSuccessReward) {
        this.refuelSuccessReward = refuelSuccessReward;
    }

    public double getSupportedPatrolReward() {
        return supportedPatrolReward;
    }

    public void setSupportedPatrolReward(double supportedPatrolReward) {
        this.supportedPatrolReward = supportedPatrolReward;
    }

    public double getFuelDeliveredReward() {
        return fuelDeliveredReward;
    }

    public void setFuelDeliveredReward(double fuelDeliveredReward) {
        this.fuelDeliveredReward = fuelDeliveredReward;
    }
}
