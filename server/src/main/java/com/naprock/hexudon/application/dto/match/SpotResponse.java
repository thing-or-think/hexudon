package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.domain.model.map.UdonType;

public record SpotResponse(
        int brand,
        int pos,
        int stocks
) {

    public SpotResponse {
        UdonType.fromValue(brand);

        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (stocks < 0) {
            throw new IllegalArgumentException("stocks must not be negative");
        }
    }
}