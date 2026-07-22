package com.naprock.hexudon.domain.model.board;

import com.naprock.hexudon.domain.model.geometry.Coordinate;


import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

/**
 * Immutable board cell.
 *
 * <p>A cell represents a single location on the game board, identified by its
 * coordinate and terrain brand.</p>
 *
 * @param coordinate the cell coordinate
 * @param terrainType the terrain brand of the cell
 */
public record Cell(
        Coordinate coordinate,
        TerrainType terrainType
) {

    /**
     * Creates a cell.
     *
     * @throws NullPointerException if {@code coordinate} or {@code terrainType} is {@code null}
     */
    public Cell {
        requireNonNull(coordinate, "coordinate");
        requireNonNull(terrainType, "terrainType");
    }

    /**
     * Creates a cell from its board coordinates and terrain value.
     *
     * @param x the zero-based column index
     * @param y the zero-based row index
     * @param value the terrain identifier
     */
    public Cell(int x, int y, int value) {
        this(new Coordinate(x, y), TerrainType.fromValue(value));
    }

    /**
     * Returns whether this cell can be traversed by an agent.
     *
     * @return {@code true} if the terrain is walkable; {@code false} otherwise
     */
    public boolean isWalkable() {
        return terrainType != TerrainType.POND;
    }
}