package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents an Udon shop on the game board.
 *
 * <p>A spot contains the shop brand identifier, its location,
 * and the remaining number of available servings.
 *
 * @param brand the shop brand identifier
 * @param coordinate the location of the shop
 * @param stocks the remaining number of Udon servings
 */
public record Spot(
        int brand,
        Coordinate coordinate,
        int stocks
) {

    /**
     * Creates a new {@code Spot}.
     *
     * @throws NullPointerException if {@code coordinate} is {@code null}
     * @throws IllegalArgumentException if {@code brand} or {@code stocks} is negative
     */
    public Spot {
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        if (brand < 0) {
            throw new IllegalArgumentException("brand must not be negative");
        }

        if (stocks < 0) {
            throw new IllegalArgumentException("stocks must not be negative");
        }
    }
}