package com.example.dqn.core.reward;

/**
 * Enum defining the different components/types of reward and penalties.
 */
public enum RewardType {
    UDON_COLLECTED,
    UDON_MULTIPLIER,
    FUEL_CONSUMED,
    INVALID_ACTION,
    IDLE,
    SUCCESSFUL_COLLECTION,
    COOPERATION,
    REFUEL_SUCCESS,
    SUPPORTED_PATROL,
    FUEL_DELIVERED
}
