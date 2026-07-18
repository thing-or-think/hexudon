package com.naprock.hexudon.sdk.model;

/**
 * Represents the role of an agent in the Hexudon game.
 * <p>
 * Each agent type is mapped to the integer value defined by the
 * Hexudon server protocol.
 */
public enum AgentType {

    /**
     * Patrol agent.
     * <p>
     * Collects Udon resources and consumes fuel while moving.
     */
    PATROL(0),

    /**
     * Refuel agent.
     * <p>
     * Supplies fuel to patrol agents and does not consume fuel when moving.
     */
    REFUEL(1);

    private final int value;

    AgentType(int value) {
        this.value = value;
    }

    /**
     * Returns the protocol integer value of this agent type.
     *
     * @return protocol value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the agent type corresponding to the protocol value.
     *
     * @param value protocol value
     * @return matching agent type
     * @throws IllegalArgumentException if the value is invalid
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

    /**
     * Returns the agent type corresponding to the string value.
     *
     * @param value the string representation ("patrol" or "refuel")
     * @return matching agent type
     */
    public static AgentType fromString(String value) {
        if (value == null) {
            return PATROL;
        }
        return switch (value.trim().toLowerCase()) {
            case "patrol" -> PATROL;
            case "refuel" -> REFUEL;
            default -> PATROL;
        };
    }
}
