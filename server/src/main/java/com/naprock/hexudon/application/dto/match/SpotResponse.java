package com.naprock.hexudon.application.dto.match;

public record SpotResponse(
        int brand,
        int pos,
        int stocks
) {

    public SpotResponse {
        if (brand < 0) {
            throw new IllegalArgumentException("brand must not be negative");
        }

        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (stocks < 0) {
            throw new IllegalArgumentException("stocks must not be negative");
        }
    }
}