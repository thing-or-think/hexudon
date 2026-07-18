package com.thingorthink.hexudon.sdk.model;

/**
 * Traffic congestion level of a road tile.
 */
public enum TrafficLevel {

    /**
     * Smooth traffic (1x movement cost).
     */
    SMOOTH(0, 1),

    /**
     * Congested traffic (2x movement cost).
     */
    CONGESTED(1, 2),

    /**
     * Traffic jam (4x movement cost).
     */
    JAM(2, 4);

    private final int value;
    private final int costMultiplier;

    TrafficLevel(int value, int costMultiplier) {
        this.value = value;
        this.costMultiplier = costMultiplier;
    }

    /**
     * Returns the protocol value of this traffic level.
     *
     * @return protocol value
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
     * Returns the traffic level corresponding to the given protocol value.
     *
     * @param value protocol value
     * @return matching traffic level
     * @throws IllegalArgumentException if the value is not supported
     */
    public static TrafficLevel fromValue(int value) {
        for (TrafficLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }

        throw new IllegalArgumentException(
                "Unknown traffic level: " + value
        );
    }
}
