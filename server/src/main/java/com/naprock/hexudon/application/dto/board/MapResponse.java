package com.naprock.hexudon.application.dto.board;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record MapResponse(
        int height,
        int width,
        List<List<Integer>> cells
) {

    public MapResponse {
        requirePositive(height, "height");
        requirePositive(width, "width");
        requireNonNull(cells, "cells");

        requireTrue(
                cells.size() == height,
                "cells rows size must equal height"
        );

        for (List<Integer> row : cells) {
            requireNonNull(row, "cell row");

            requireTrue(
                    row.size() == width,
                    "each row size must equal width"
            );

            for (Integer cell : row) {
                requireNonNull(cell, "cell");

                requireTrue(
                        cell >= 0 && cell <= 3,
                        "cell value must be between 0 and 3"
                );
            }
        }

        // Deep immutable copy
        cells = cells.stream()
                .map(List::copyOf)
                .toList();
    }
}