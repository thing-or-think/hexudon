package com.naprock.hexudon.application.dto.match;

import jakarta.validation.constraints.Min;

public record CoordinateRequest(
        @Min(value = 0, message = "Coordinate x must not be negative")
        int x,

        @Min(value = 0, message = "Coordinate y must not be negative")
        int y
) {
}