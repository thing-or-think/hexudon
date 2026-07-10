package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.TerrainType;

public record CellResponse(
        CoordinateResponse coordinate,
        TerrainType terrainType
) {
}