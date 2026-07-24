package com.example.dqn.feature.hexworld.domain;

/**
 * Domain record representing an Udon Spot on the HexWorld map.
 * Enforces non-negative brand ID and stocks, and non-null position.
 *
 * @param brand the brand identifier of the Udon spot (must be >= 0).
 * @param position the HexPosition coordinate of the spot.
 * @param stocks the quantity of Udon available at this spot (must be >= 0).
 */
public record UdonSpot(
    int brand,
    HexPosition position,
    int stocks
) {
    public UdonSpot {
        if (brand < 0) {
            throw new IllegalArgumentException("brand must not be negative: " + brand);
        }
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        if (stocks < 0) {
            throw new IllegalArgumentException("stocks must not be negative: " + stocks);
        }
    }
}
