package com.naprock.hexudon.dto;

import com.naprock.hexudon.domain.valueobject.Cell;
import com.naprock.hexudon.domain.valueobject.TerrainType;

public record CellResponse(
        int x,
        int y,
        TerrainType terrainType
) {

    public CellResponse(Cell cell) {
        this(
                cell.getX(),
                cell.getY(),
                cell.getTerrainType()
        );
    }
}