package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents a single cell on the game board.
 *
 * <p>A cell consists of its coordinate and terrain type.
 *
 * @param coordinate the cell coordinate
 * @param terrain the terrain type of the cell
 */
public record Cell(
        Coordinate coordinate,
        TerrainType terrain
) {

    /**
     * Creates a new cell.
     *
     * @throws NullPointerException if {@code coordinate} or {@code terrain} is {@code null}
     */
    public Cell {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(terrain, "terrain must not be null");
    }
}