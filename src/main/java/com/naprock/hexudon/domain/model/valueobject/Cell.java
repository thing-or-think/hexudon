package com.naprock.hexudon.domain.model.valueobject;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.Objects;

public class Cell {

    private final Coordinate coordinate;
    private final TerrainType terrainType;

    public Cell(Coordinate coordinate, TerrainType terrainType) {
        validateNotNull(coordinate, "coordinate");
        validateNotNull(terrainType, "terrainType");

        this.coordinate = coordinate;
        this.terrainType = terrainType;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    /**
     * Returns true if this cell can be traversed by an agent.
     */
    public boolean isWalkable() {
        return terrainType != TerrainType.POND;
    }

    private void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Cell other)) {
            return false;
        }

        return Objects.equals(coordinate, other.coordinate)
                && terrainType == other.terrainType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate, terrainType);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "coordinate=" + coordinate +
                ", terrainType=" + terrainType +
                '}';
    }
}
