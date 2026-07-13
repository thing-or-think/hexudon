package com.naprock.hexudon.application.dto;


import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.Objects;

/**
 * Response DTO representing the static configuration of a single map cell.
 *
 * <p>Each map cell consists of its coordinate and terrain type.
 * This DTO is included in {@link MatchConfigResponse} as part of the
 * complete map configuration returned to the client.</p>
 */
public record MapCellConfigResponse(
        CoordinateResponse coordinate,
        TerrainType terrainType
) {

    /**
     * Creates an immutable map cell configuration response.
     *
     * <p>This constructor validates that all required fields are not
     * {@code null}.</p>
     *
     * @param coordinate the coordinate of the map cell
     * @param terrainType the terrain type of the map cell
     * @throws NullPointerException if {@code coordinate} or {@code terrainType} is {@code null}
     */
    public MapCellConfigResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(terrainType, "terrainType must not be null");
    }
}