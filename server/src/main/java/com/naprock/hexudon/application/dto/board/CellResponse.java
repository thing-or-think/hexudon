package com.naprock.hexudon.application.dto.board;


import com.naprock.hexudon.domain.model.map.TerrainType;

import java.util.Objects;

public record CellResponse(
        CoordinateResponse coordinate,
        TerrainType terrainType
) {

    public CellResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(terrainType, "terrainType must not be null");

    }
}