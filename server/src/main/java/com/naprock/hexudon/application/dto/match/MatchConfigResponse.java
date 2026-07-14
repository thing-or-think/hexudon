package com.naprock.hexudon.application.dto.match;

import java.util.List;
import java.util.Objects;

public record MatchConfigResponse(
        int mapWidth,
        int mapHeight,
        List<CellResponse> cells,
        List<SpotResponse> spots,
        int agentsPerTeam,
        int maxFuel,
        int maxStepsPerTurn,
        int maxTurn
) {

    public MatchConfigResponse {

        cells = List.copyOf(
                Objects.requireNonNull(cells, "cells must not be null")
        );

        spots = List.copyOf(
                Objects.requireNonNull(spots, "spots must not be null")
        );

        if (mapWidth <= 0) {
            throw new IllegalArgumentException("mapWidth must be positive");
        }

        if (mapHeight <= 0) {
            throw new IllegalArgumentException("mapHeight must be positive");
        }

        if (agentsPerTeam <= 0) {
            throw new IllegalArgumentException("agentsPerTeam must be positive");
        }

        if (maxFuel <= 0) {
            throw new IllegalArgumentException("maxFuel must be positive");
        }

        if (maxStepsPerTurn <= 0) {
            throw new IllegalArgumentException("maxStepsPerTurn must be positive");
        }

        if (maxTurn <= 0) {
            throw new IllegalArgumentException("maxTurn must be positive");
        }
    }
}