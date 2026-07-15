package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record CollectResult(
        boolean success,
        int teamId,
        Coordinate coordinate,
        Integer type
) {

    public CollectResult {

        requireNonNegative(teamId, "teamId");
        requireNonNull(coordinate, "coordinate");

        if (success) {
            requireNonNull(type, "type");
            requireTrue(
                    type >= 0 && type <= 3,
                    "type must be between 0 and 3"
            );
        } else if (type != null) {
            throw new IllegalArgumentException(
                    "type must be null when collect failed"
            );
        }
    }

    public static CollectResult success(
            int teamId,
            Coordinate coordinate,
            int type
    ) {
        return new CollectResult(
                true,
                teamId,
                coordinate,
                type
        );
    }

    public static CollectResult failed(
            int teamId,
            Coordinate coordinate
    ) {
        return new CollectResult(
                false,
                teamId,
                coordinate,
                null
        );
    }
}