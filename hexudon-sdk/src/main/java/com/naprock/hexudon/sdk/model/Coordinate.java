package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents a coordinate on an Odd-R offset hexagonal grid.
 *
 * @param pos linear index of the cell
 * @param x column coordinate
 * @param y row coordinate
 */
public record Coordinate(int pos, int x, int y) {

    /**
     * Creates a coordinate from a linear position.
     *
     * @param pos linear position
     * @param width board width
     */
    public Coordinate(int pos, int width) {
        this(pos, pos % width, pos / width);
    }

    /**
     * Returns the hex distance to another coordinate.
     *
     * @param other target coordinate
     * @return minimum number of hex moves
     */
    public int getDistance(Coordinate other) {
        Objects.requireNonNull(other, "other must not be null");

        Cube a = toCube(this);
        Cube b = toCube(other);

        return (Math.abs(a.x - b.x)
                + Math.abs(a.y - b.y)
                + Math.abs(a.z - b.z)) / 2;
    }

    /**
     * Returns the neighboring coordinate in the specified direction.
     *
     * @param direction movement direction
     * @param width board width
     * @return neighboring coordinate
     */
    public Coordinate getNeighbor(Direction direction, int width) {
        Objects.requireNonNull(direction, "direction must not be null");

        boolean oddRow = (y & 1) == 1;

        int nextX = x;
        int nextY = y;

        if (oddRow) {
            switch (direction) {
                case UP_RIGHT -> {
                    nextX = x + 1;
                    nextY = y - 1;
                }
                case RIGHT -> nextX = x + 1;
                case DOWN_RIGHT -> {
                    nextX = x + 1;
                    nextY = y + 1;
                }
                case DOWN_LEFT -> nextY = y + 1;
                case LEFT -> nextX = x - 1;
                case UP_LEFT -> nextY = y - 1;
            }
        } else {
            switch (direction) {
                case UP_RIGHT -> nextY = y - 1;
                case RIGHT -> nextX = x + 1;
                case DOWN_RIGHT -> nextY = y + 1;
                case DOWN_LEFT -> {
                    nextX = x - 1;
                    nextY = y + 1;
                }
                case LEFT -> nextX = x - 1;
                case UP_LEFT -> {
                    nextX = x - 1;
                    nextY = y - 1;
                }
            }
        }

        int nextPos = nextY * width + nextX;

        return new Coordinate(nextPos, nextX, nextY);
    }

    /**
     * Converts an Odd-R coordinate to cube coordinates.
     */
    private static Cube toCube(Coordinate coordinate) {
        int cubeX = coordinate.x - (coordinate.y - (coordinate.y & 1)) / 2;
        int cubeZ = coordinate.y;
        int cubeY = -cubeX - cubeZ;

        return new Cube(cubeX, cubeY, cubeZ);
    }

    /**
     * Internal cube coordinate representation.
     */
    private record Cube(int x, int y, int z) {
    }
}