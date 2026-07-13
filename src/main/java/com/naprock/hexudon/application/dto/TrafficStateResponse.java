package com.naprock.hexudon.application.dto;

import java.util.Objects;

/**
 * Response DTO representing the traffic state of a road cell.
 *
 * @param coordinate   road cell coordinate
 * @param trafficLevel traffic level as a String (e.g. "Smooth", "Busy", "Congested")
 */
public record TrafficStateResponse(
        CoordinateResponse coordinate,
        String trafficLevel
) {

    public TrafficStateResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(trafficLevel, "trafficLevel must not be null");
    }
}