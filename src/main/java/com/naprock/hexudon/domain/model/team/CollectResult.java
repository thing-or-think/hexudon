package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.UdonType;

import java.util.Objects;

public record CollectResult(
        boolean success,
        String teamName,
        Coordinate coordinate,
        UdonType type
) {

    public CollectResult {
        Objects.requireNonNull(teamName, "teamName must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        if (success) {
            Objects.requireNonNull(type, "type must not be null");
        }
    }

    public static CollectResult success(
            String agentId,
            Coordinate coordinate,
            UdonType type
    ) {
        return new CollectResult(true, agentId, coordinate, type);
    }

    public static CollectResult failed(
            String agentId,
            Coordinate coordinate
    ) {
        return new CollectResult(false, agentId, coordinate, null);
    }
}