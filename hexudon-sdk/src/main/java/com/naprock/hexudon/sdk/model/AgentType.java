package com.naprock.hexudon.sdk.model;

/**
 * Represents the role and behavior type of an Agent in Hexudon game.
 *
 * <p>
 * PATROL Agent:
 * - Collects Udon (noodle resource).
 * - Consumes fuel when moving.
 *
 * <p>
 * REFUEL Agent:
 * - Provides fuel to Patrol Agents.
 * - Does not consume fuel when moving.
 *
 * <p>
 * The integer value must match the game server protocol.
 */
public enum AgentType {

    /**
     * Patrol agent that collects resources and consumes fuel.
     */
    PATROL(0),

    /**
     * Refuel agent that restores fuel for Patrol agents.
     */
    REFUEL(1);

    private final int value;

    AgentType(int value) {
        this.value = value;
    }

    /**
     * Returns the integer representation used by the game server.
     *
     * @return agent type value
     */
    public int getValue() {
        return value;
    }

    /**
     * Converts an integer value from API response/request
     * into corresponding AgentType.
     *
     * @param value integer representation
     * @return matching AgentType
     * @throws IllegalArgumentException if value is unknown
     */
    public static AgentType fromValue(int value) {
        for (AgentType type : values()) {
            if (type.value == value) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unknown agent type value: " + value
        );
    }
}