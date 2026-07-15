package com.naprock.hexudon.domain.model.geometry;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;
import static com.naprock.hexudon.domain.validation.DomainValidator.requirePositive;

public record Coordinate(
        int x,
        int y
) {

    public Coordinate {
        requireNonNegative(x, "x");
        requireNonNegative(y, "y");
    }

    /**
     * Converts this coordinate to a row-major index.
     */
    public int toIndex(int width) {
        requirePositive(width, "width");
        return y * width + x;
    }

    /**
     * Creates a coordinate from a row-major index.
     */
    public static Coordinate create(int index, int width) {
        requireNonNegative(index, "index");
        requirePositive(width, "width");

        return new Coordinate(
                index % width,
                index / width
        );
    }

    /**
     * Returns whether the specified coordinate is adjacent on an odd-r offset
     * hexagonal grid.
     */
    public boolean isAdjacentTo(Coordinate other) {

        requireNonNull(other, "other");

        int deltaX = other.x - x;
        int deltaY = other.y - y;

        if (deltaX == 0 && deltaY == 0) {
            return false;
        }

        if ((y & 1) == 1) {
            return switch (deltaY) {
                case -1 -> deltaX == 0 || deltaX == 1;
                case 0 -> deltaX == -1 || deltaX == 1;
                case 1 -> deltaX == 0 || deltaX == 1;
                default -> false;
            };
        }

        return switch (deltaY) {
            case -1 -> deltaX == -1 || deltaX == 0;
            case 0 -> deltaX == -1 || deltaX == 1;
            case 1 -> deltaX == -1 || deltaX == 0;
            default -> false;
        };
    }

    /**
     * Calculates the hex distance to another coordinate.
     */
    public int distanceTo(Coordinate other) {

        requireNonNull(other, "other");

        CubeCoordinate source = toCubeCoordinate();
        CubeCoordinate target = other.toCubeCoordinate();

        int deltaX = Math.abs(source.x() - target.x());
        int deltaY = Math.abs(source.y() - target.y());
        int deltaZ = Math.abs(source.z() - target.z());

        return Math.max(
                deltaX,
                Math.max(deltaY, deltaZ)
        );
    }

    /**
     * Returns the neighboring coordinate in the specified direction.
     */
    public Coordinate getNeighbor(Direction direction) {

        requireNonNull(direction, "direction");

        return new Coordinate(
                x + direction.getDx(y),
                y + direction.getDy(y)
        );
    }

    /**
     * Converts this odd-r offset coordinate to cube coordinates.
     */
    private CubeCoordinate toCubeCoordinate() {

        int cubeX = x - (y - (y & 1)) / 2;
        int cubeZ = y;
        int cubeY = -cubeX - cubeZ;

        return new CubeCoordinate(
                cubeX,
                cubeY,
                cubeZ
        );
    }

    /**
     * Cube coordinate representation for hex distance calculations.
     */
    private record CubeCoordinate(
            int x,
            int y,
            int z
    ) {
    }
}