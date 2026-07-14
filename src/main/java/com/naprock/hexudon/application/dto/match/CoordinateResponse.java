package com.naprock.hexudon.application.dto.match;

public record CoordinateResponse(
        int x,
        int y
) {
    public CoordinateResponse {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException(
                    "Coordinate x and y must not be negative"
            );
        }
    }
}