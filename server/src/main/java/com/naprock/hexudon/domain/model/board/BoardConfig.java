package com.naprock.hexudon.domain.model.board;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

/**
 * Immutable configuration describing the layout of a game board.
 *
 * @param width  number of columns, must be positive
 * @param height number of rows, must be positive
 * @param cells  terrain grid
 * @param spots  immutable list of spot configurations
 */
public record BoardConfig(
        int width,
        int height,
        List<List<Integer>> cells,
        List<SpotConfig> spots
) {

    public BoardConfig {
        requirePositive(width, "width");
        requirePositive(height, "height");

        requireNonNull(cells, "cells");
        requireNonNull(spots, "spots");

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

        // Immutable copy
        spots = List.copyOf(spots);
    }

    /**
     * Returns the terrain identifier at the specified coordinate.
     *
     * @param x zero-based column index
     * @param y zero-based row index
     * @return terrain identifier
     * @throws IllegalArgumentException if the coordinate is outside the board
     */
    public int get(int x, int y) {
        requireTrue(
                x >= 0 && x < width,
                "x must be between 0 and " + (width - 1)
        );

        requireTrue(
                y >= 0 && y < height,
                "y must be between 0 and " + (height - 1)
        );

        return cells.get(y).get(x);
    }
}