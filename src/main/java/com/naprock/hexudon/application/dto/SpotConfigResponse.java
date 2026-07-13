package com.naprock.hexudon.application.dto;

import java.util.Objects;

/**
 * Response DTO representing the static configuration of a resource spot
 * on the game map.
 *
 * <p>Each spot contains its coordinate and the type of Udon resource
 * available at that location. This DTO is included in
 * {@link MatchConfigResponse} as part of the complete map configuration
 * returned to the client.</p>
 */
public record SpotConfigResponse(
        CoordinateResponse coordinate,
        String udonType
) {

    /**
     * Creates an immutable spot configuration response.
     *
     * <p>This constructor validates that all required fields are not
     * {@code null}.</p>
     *
     * @param coordinate the coordinate of the resource spot
     * @param udonType the name of the Udon type available at the spot
     * @throws NullPointerException if {@code coordinate} or {@code udonType} is {@code null}
     */
    public SpotConfigResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(udonType, "udonType must not be null");
    }
}