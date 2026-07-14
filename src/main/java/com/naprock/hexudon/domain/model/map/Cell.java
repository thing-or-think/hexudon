package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.Objects;

public record Cell(
        Coordinate coordinate,
        TerrainType terrainType
) {

    public Cell {
        validateNotNull(coordinate, "coordinate");
        validateNotNull(terrainType, "terrainType");
    }

    /**
     * Returns true if this cell can be traversed by an agent.
     */
    public boolean isWalkable() {
        return terrainType != TerrainType.POND;
    }

    private static void validateNotNull(
            Object value,
            String fieldName
    ) {
        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }
}