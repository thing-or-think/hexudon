package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record CollectResult(
        boolean success,
        String teamId,
        Coordinate coordinate,
        Integer brand
) {

    public CollectResult {

        requireNotBlank(teamId, "teamId");
        requireNonNull(coordinate, "coordinate");

        if (success) {
            requireNonNull(brand, "type");
            requireTrue(
                    brand >= 0 && brand <= 3,
                    "brand must be between 0 and 3"
            );
        } else if (brand != null) {
            throw new IllegalArgumentException(
                    "brand must be null when collect failed"
            );
        }
    }

    public static CollectResult success(
            String teamId,
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
            String teamId,
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