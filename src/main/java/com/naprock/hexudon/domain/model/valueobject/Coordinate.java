package com.naprock.hexudon.domain.model.valueobject;


import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Objects;

/**
 * Value Object representing a coordinate on a hexagonal grid
 * using Odd-R horizontal offset coordinate system.
 */
public final class Coordinate {

    private final int x;
    private final int y;

    /**
     * Creates an immutable coordinate.
     *
     * @param x column index
     * @param y row index
     * @throws GameRuleViolationException if x or y is negative
     */
    public Coordinate(int x, int y) {
        validateCoordinate(x, y);

        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Checks whether another coordinate is adjacent.
     *
     * @param other target coordinate
     * @return true if hex distance equals 1
     */
    public boolean isAdjacentTo(Coordinate other) {
        validateOther(other);

        int dx = other.x - this.x;
        int dy = other.y - this.y;

        if (dx == 0 && dy == 0) {
            return false;
        }

        if (this.y % 2 != 0) {
            // Odd row
            return switch (dy) {
                case -1 -> dx == 0 || dx == 1;
                case 0 -> dx == -1 || dx == 1;
                case 1 -> dx == 0 || dx == 1;
                default -> false;
            };
        } else {
            // Even row
            return switch (dy) {
                case -1 -> dx == -1 || dx == 0;
                case 0 -> dx == -1 || dx == 1;
                case 1 -> dx == -1 || dx == 0;
                default -> false;
            };
        }
    }


    /**
     * Calculates minimum hexagonal distance between two coordinates.
     * Conversion:
     * Odd-R offset -> Cube coordinate
     * distance = max(
     *      abs(dx),
     *      abs(dy),
     *      abs(dz)
     * )
     *
     * @param other destination coordinate
     * @return minimum movement steps
     */
    public int distanceTo(Coordinate other) {
        validateOther(other);

        CubeCoordinate a = toCube();
        CubeCoordinate b = other.toCube();

        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        int dz = Math.abs(a.z - b.z);

        return Math.max(dx, Math.max(dy, dz));
    }


    /**
     * Returns neighbor coordinate in given direction.
     *
     * @param direction movement direction
     * @return new coordinate
     */
    public Coordinate getNeighbor(Direction direction) {

        if (direction == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Direction cannot be null"
            );
        }

        int dx = direction.getDx(this.y);
        int dy = direction.getDy(this.y);

        return new Coordinate(
                this.x + dx,
                this.y + dy
        );
    }


    /**
     * Converts Odd-R offset coordinate to Cube coordinate.
     */
    private CubeCoordinate toCube() {

        int cubeX = x - (y - (y & 1)) / 2;
        int cubeZ = y;
        int cubeY = -cubeX - cubeZ;

        return new CubeCoordinate(
                cubeX,
                cubeY,
                cubeZ
        );
    }


    private static void validateCoordinate(int x, int y) {

        if (x < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate x cannot be negative: " + x
            );
        }

        if (y < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate y cannot be negative: " + y
            );
        }
    }


    private static void validateOther(Coordinate other) {

        if (other == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate cannot be null"
            );
        }
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Coordinate other)) {
            return false;
        }

        return this.x == other.x
                && this.y == other.y;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }


    /**
     * Internal immutable cube coordinate.
     */
    private record CubeCoordinate(
            int x,
            int y,
            int z
    ) {
    }
}