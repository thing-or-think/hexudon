package com.naprock.hexudon.sdk.model;

/**
 * Traffic congestion level of a road tile.
 */
public enum TrafficLevel {

    /** Normal traffic (1x movement cost). */
    NORMAL(0, 1),

    /** Busy traffic (2x movement cost). */
    BUSY(1, 2),

    /** Congested traffic (4x movement cost). */
    CONGESTED(2, 4);

    private final int value;
    private final int costMultiplier;

    TrafficLevel(int value, int costMultiplier) {
        this.value = value;
        this.costMultiplier = costMultiplier;
    }

    /**
     * Returns the numeric traffic value.
     *
     * @return traffic value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the movement cost multiplier.
     *
     * @return movement cost multiplier
     */
    public int getCostMultiplier() {
        return costMultiplier;
    }

    /**
     * Returns the traffic level for the given value.
     *
     * @param value traffic value
     * @return matching traffic level
     * @throws IllegalArgumentException if the value is invalid
     */
    public static TrafficLevel fromValue(int value) {
        for (TrafficLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown traffic level: " + value);
    }
}