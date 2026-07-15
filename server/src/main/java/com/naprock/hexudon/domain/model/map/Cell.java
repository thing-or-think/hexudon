package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.Objects;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public record Cell(
        Coordinate coordinate,
        TerrainType terrainType
) {

    public Cell {
        requireNonNull(coordinate, "coordinate");
        requireNonNull(terrainType, "terrainType");
    }

    /**
     * Returns true if this cell can be traversed by an agent.
     */
    public boolean isWalkable() {
        return terrainType != TerrainType.POND;
    }
}