package com.example.dqn.feature.hexworld.domain;

/**
 * Immutable record representing a 2D grid position (column x, row y) in HexWorld.
 * Enforces non-negative coordinate boundaries.
 */
public record HexPosition(int x, int y) {

    /**
     * Constructs a HexPosition and validates coordinate non-negativity.
     *
     * @param x the column index (must be >= 0).
     * @param y the row index (must be >= 0).
     */
    public HexPosition {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("HexPosition coordinates must be non-negative: (" + x + ", " + y + ")");
        }
    }
}
