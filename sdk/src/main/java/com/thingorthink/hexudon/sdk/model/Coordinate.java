package com.thingorthink.hexudon.sdk.model;

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

    private static final String WIDTH_ERROR =
            "width must be greater than 0";

    /**
     * Creates a coordinate.
     */
    public Coordinate {
        requireNonNegative(pos, "pos");
        requireNonNegative(x, "x");
        requireNonNegative(y, "y");
    }

    /**
     * Creates a coordinate from a linear position.
     */
    public Coordinate(int pos, int width) {
        this(
                pos,
                positionToX(pos, width),
                positionToY(pos, width)
        );
    }

    /**
     * Returns the minimum hex distance to another coordinate.
     */
    public int distanceTo(Coordinate other) {
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
     */
    public Coordinate getNeighbor(
            Direction direction,
            int width
    ) {
        Objects.requireNonNull(direction, "direction must not be null");

        if (width <= 0) {
            throw new IllegalArgumentException(WIDTH_ERROR);
        }

        int nextX = x;
        int nextY = y;

        boolean oddRow = isOddRow();

        switch (direction) {
            case UP_RIGHT -> {
                nextY--;
                if (oddRow) nextX++;
            }

            case RIGHT -> nextX++;

            case DOWN_RIGHT -> {
                nextY++;
                if (oddRow) nextX++;
            }

            case DOWN_LEFT -> {
                nextY++;
                if (!oddRow) nextX--;
            }

            case LEFT -> nextX--;

            case UP_LEFT -> {
                nextY--;
                if (!oddRow) nextX--;
            }
        }

        return new Coordinate(
                toPosition(nextX, nextY, width),
                nextX,
                nextY
        );
    }

    /**
     * Returns whether this coordinate is on an odd row.
     */
    public boolean isOddRow() {
        return (y & 1) == 1;
    }

    /**
     * Converts x/y into a linear position.
     */
    public static int toPosition(
            int x,
            int y,
            int width
    ) {
        if (width <= 0) {
            throw new IllegalArgumentException(WIDTH_ERROR);
        }

        return y * width + x;
    }

    private static int positionToX(
            int pos,
            int width
    ) {
        if (width <= 0) {
            throw new IllegalArgumentException(WIDTH_ERROR);
        }

        return pos % width;
    }

    private static int positionToY(
            int pos,
            int width
    ) {
        if (width <= 0) {
            throw new IllegalArgumentException(WIDTH_ERROR);
        }

        return pos / width;
    }

    private static Cube toCube(Coordinate coordinate) {
        int cubeX =
                coordinate.x
                        - (coordinate.y - (coordinate.y & 1)) / 2;

        int cubeZ = coordinate.y;
        int cubeY = -cubeX - cubeZ;

        return new Cube(cubeX, cubeY, cubeZ);
    }

    private static void requireNonNegative(
            int value,
            String name
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    name + " must be >= 0"
            );
        }
    }

    /**
     * Cube coordinate used internally for distance calculation.
     */
    private record Cube(
            int x,
            int y,
            int z
    ) {
    }
}
