package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents the Hexudon game board.
 *
 * <p>A board consists of a two-dimensional matrix of {@link Cell} objects
 * arranged using the game's hexagonal coordinate system.
 *
 * <p>This record is immutable. The internal cell matrix is defensively copied
 * during construction and whenever it is returned.
 *
 * @param width the number of columns
 * @param height the number of rows
 * @param cells the board cell matrix
 */
public record Board(
        int width,
        int height,
        Cell[][] cells
) {

    /**
     * Creates a new board.
     *
     * @throws IllegalArgumentException if the dimensions are invalid or the
     *                                  cell matrix does not match the specified
     *                                  width and height
     * @throws NullPointerException if {@code cells} is {@code null}
     */
    public Board {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }

        Objects.requireNonNull(cells, "cells must not be null");

        if (cells.length != height) {
            throw new IllegalArgumentException("cells height does not match");
        }

        for (Cell[] row : cells) {
            if (row == null || row.length != width) {
                throw new IllegalArgumentException("cells width does not match");
            }
        }

        cells = deepCopy(cells);
    }

    /**
     * Returns a defensive copy of the board cell matrix.
     *
     * <p>Modifying the returned array does not affect this board.
     *
     * @return a deep copy of the cell matrix
     */
    @Override
    public Cell[][] cells() {
        return deepCopy(cells);
    }

    /**
     * Returns the cell at the specified coordinate.
     *
     * @param coordinate the coordinate to look up
     * @return the cell at the given coordinate
     * @throws NullPointerException if {@code coordinate} is {@code null}
     * @throws IllegalArgumentException if the coordinate is outside the board
     */
    public Cell getCell(Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        if (!isValidCoordinate(coordinate)) {
            throw new IllegalArgumentException(
                    "Coordinate is outside the board: " + coordinate);
        }

        return cells[coordinate.y()][coordinate.x()];
    }

    /**
     * Determines whether a coordinate is inside the board.
     *
     * @param coordinate the coordinate to validate
     * @return {@code true} if the coordinate is inside the board;
     *         {@code false} otherwise
     */
    public boolean isValidCoordinate(Coordinate coordinate) {
        return coordinate != null
                && coordinate.x() >= 0
                && coordinate.x() < width
                && coordinate.y() >= 0
                && coordinate.y() < height;
    }

    /**
     * Creates a defensive copy of the given cell matrix.
     *
     * @param source the source matrix
     * @return a copied matrix
     */
    private static Cell[][] deepCopy(Cell[][] source) {
        Cell[][] copy = new Cell[source.length][];

        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }

        return copy;
    }
}