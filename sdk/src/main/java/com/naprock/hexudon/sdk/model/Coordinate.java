package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Immutable coordinate on an Odd-R offset hexagonal grid.
 *
 * @param pos linear position
 * @param x   column index
 * @param y   row index
 */
public record Coordinate(
        int pos,
        int x,
        int y
) {

    /**
     * Creates a coordinate.
     *
     * @param pos linear position
     * @param x   column
     * @param y   row
     */
    public Coordinate {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must be >= 0");
        }
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
    }

    /**
     * Creates a coordinate from a linear position.
     *
     * @param pos   linear position
     * @param width board width
     */
    public Coordinate(int pos, int width) {
        this(
                pos,
                pos % width,
                pos / width
        );

        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
    }

    /**
     * Calculates the minimum hex distance.
     *
     * @param other destination coordinate
     * @return minimum number of steps
     */
    public int getDistance(Coordinate other) {
        Objects.requireNonNull(other, "other must not be null");

        Cube a = toCube(this);
        Cube b = toCube(other);

        return (
                Math.abs(a.x - b.x)
                        + Math.abs(a.y - b.y)
                        + Math.abs(a.z - b.z)
        ) / 2;
    }

    /**
     * Returns the neighboring coordinate in the given direction.
     *
     * @param direction movement direction
     * @param width     board width
     * @return neighboring coordinate
     */
    public Coordinate getNeighbor(Direction direction, int width) {
        Objects.requireNonNull(direction, "direction must not be null");

        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }

        boolean oddRow = (y & 1) == 1;

        int nextX = x;
        int nextY = y;

        switch (direction) {
            case UP_RIGHT -> {
                nextY--;
                if (oddRow) {
                    nextX++;
                }
            }

            case RIGHT -> nextX++;

            case DOWN_RIGHT -> {
                nextY++;
                if (oddRow) {
                    nextX++;
                }
            }

            case DOWN_LEFT -> {
                nextY++;
                if (!oddRow) {
                    nextX--;
                }
            }

            case LEFT -> nextX--;

            case UP_LEFT -> {
                nextY--;
                if (!oddRow) {
                    nextX--;
                }
            }
        }

        int nextPos = nextY * width + nextX;

        return new Coordinate(nextPos, nextX, nextY);
    }

    /**
     * Converts an Odd-R coordinate into Cube coordinates.
     */
    private static Cube toCube(Coordinate coordinate) {
        int cubeX =
                coordinate.x - (coordinate.y - (coordinate.y & 1)) / 2;

        int cubeZ = coordinate.y;
        int cubeY = -cubeX - cubeZ;

        return new Cube(cubeX, cubeY, cubeZ);
    }

    /**
     * Internal cube coordinate representation.
     */
    private record Cube(
            int x,
            int y,
            int z
    ) {
    }
}