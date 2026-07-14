package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.domain.model.map.UdonType;

import java.util.Objects;

public record SpotResponse(
        CoordinateResponse coordinate,
        UdonType udonType,
        int amount
) {

    public SpotResponse {
        Objects.requireNonNull(coordinate, "coordinate must not be null");
        Objects.requireNonNull(udonType, "udonType must not be null");

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative"
            );
        }
    }
}