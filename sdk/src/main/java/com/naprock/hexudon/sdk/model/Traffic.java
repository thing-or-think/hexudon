package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents the traffic status of a road cell.
 *
 * <p>A traffic entry associates a board coordinate with its current
 * traffic level.
 *
 * @param coordinate the road coordinate
 * @param level the traffic level at the coordinate
 */
public record Traffic(
        Coordinate coordinate,
        TrafficLevel level
) {

    /**
     * Creates a new {@code Traffic}.
     *
     * @throws NullPointerException if {@code coordinate} or {@code level} is {@code null}
     */
    public Traffic {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(level, "level must not be null");
    }
}