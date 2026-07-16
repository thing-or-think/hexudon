package com.naprock.hexudon.sdk.model;

/**
 * Represents the six movement directions on the Hexudon hexagonal grid.
 *
 * <p>Each enum constant is mapped to the direction ID defined by the
 * Hexudon Game Server protocol. The mapping must remain unchanged
 * to ensure compatibility with the server.</p>
 *
 * <pre>
 * UP_RIGHT   = 0
 * RIGHT      = 1
 * DOWN_RIGHT = 2
 * DOWN_LEFT  = 3
 * LEFT       = 4
 * UP_LEFT    = 5
 * </pre>
 */
public enum Direction {

    UP_RIGHT,
    RIGHT,
    DOWN_RIGHT,
    DOWN_LEFT,
    LEFT,
    UP_LEFT
}