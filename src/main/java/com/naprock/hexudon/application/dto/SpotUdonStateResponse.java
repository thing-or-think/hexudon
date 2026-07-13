package com.naprock.hexudon.application.dto;

import java.util.Objects;

/**
 * Response DTO representing the state of a Spot in a specific day.
 *
 * @param coordinate coordinate of the Spot
 * @param udonStock remaining Udon stock at the Spot
 */
public record SpotUdonStateResponse(
        CoordinateResponse coordinate,
        int udonStock
) {

    public SpotUdonStateResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        if (udonStock < 0) {
            throw new IllegalArgumentException("udonStock must be greater than or equal to 0");
        }
    }
}