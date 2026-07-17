package com.naprock.hexudon.sdk.model;

/**
 * Represents the congestion level of a road cell.
 *
 * <p>
 * Traffic level affects movement cost calculation:
 * <ul>
 *     <li>NORMAL: 1x movement cost</li>
 *     <li>BUSY: 2x movement cost</li>
 *     <li>CONGESTED: 4x movement cost</li>
 * </ul>
 *
 * <p>
 * The integer value must match the Hexudon game server protocol.
 */
public enum TrafficLevel {

    /**
     * Normal traffic condition.
     * Movement cost multiplier: 1x.
     */
    NORMAL(0, 1),

    /**
     * Busy traffic condition.
     * Movement cost multiplier: 2x.
     */
    BUSY(1, 2),

    /**
     * Congested traffic condition.
     * Movement cost multiplier: 4x.
     */
    CONGESTED(2, 4);

    private final int value;
    private final int costMultiplier;

    TrafficLevel(int value, int costMultiplier) {
        this.value = value;
        this.costMultiplier = costMultiplier;
    }

    /**
     * Returns the integer representation used by the game server.
     *
     * @return traffic level value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the movement cost multiplier.
     *
     * @return cost multiplier
     */
    public int getCostMultiplier() {
        return costMultiplier;
    }

    /**
     * Converts an integer value from API request/response
     * into corresponding TrafficLevel.
     *
     * @param value integer representation
     * @return matching TrafficLevel
     * @throws IllegalArgumentException if value is unknown
     */
    public static TrafficLevel fromValue(int value) {
        for (TrafficLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }

        throw new IllegalArgumentException(
                "Unknown traffic level value: " + value
        );
    }
}