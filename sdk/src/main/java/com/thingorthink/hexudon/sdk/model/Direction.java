package com.thingorthink.hexudon.sdk.model;

/**
 * Represents the six movement directions on an Odd-R hexagonal grid.
 * <p>
 * Each direction is mapped to the integer value defined by the
 * Hexudon server protocol.
 */
public enum Direction {

    /**
     * Up-left direction.
     */
    UP_LEFT(0),

    /**
     * Up-right direction.
     */
    UP_RIGHT(1),

    /**
     * Right direction.
     */
    RIGHT(2),

    /**
     * Down-right direction.
     */
    DOWN_RIGHT(3),

    /**
     * Down-left direction.
     */
    DOWN_LEFT(4),

    /**
     * Left direction.
     */
    LEFT(5);

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    /**
     * Returns the protocol integer value of this direction.
     *
     * @return protocol value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the direction corresponding to the protocol value.
     *
     * @param value protocol value
     * @return matching direction
     * @throws IllegalArgumentException if the value is invalid
     */
    public static Direction fromValue(int value) {
        for (Direction direction : values()) {
            if (direction.value == value) {
                return direction;
            }
        }

        throw new IllegalArgumentException(
                "Unknown direction value: " + value
        );
    }
}
