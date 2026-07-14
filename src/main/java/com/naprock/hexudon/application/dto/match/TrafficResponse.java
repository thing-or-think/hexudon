package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.domain.model.traffic.TrafficLevel;

import java.util.Objects;

public record TrafficResponse(
        CoordinateResponse coordinate,
        TrafficLevel trafficLevel
) {

    public TrafficResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(trafficLevel, "trafficLevel must not be null");
    }
}