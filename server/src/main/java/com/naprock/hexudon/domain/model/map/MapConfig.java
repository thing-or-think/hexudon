package com.naprock.hexudon.domain.model.map;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record MapConfig(
        int width,
        int height,
        List<List<Integer>> cells
) {

    public MapConfig {
        requirePositive(width, "width");
        requirePositive(height, "height");
        requireNonNull(cells, "cells");

        requireTrue(
                cells.size() == height,
                "cells rows size must equal height"
        );

        for (List<Integer> row : cells) {
            requireNonNull(row, "cell row");

            requireTrue(
                    row.size() == width,
                    "each cell row size must equal width"
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