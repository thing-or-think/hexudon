package com.naprock.hexudon.domain.model.geometry;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

public record Coordinate(
        int x,
        int y
) {

    public Coordinate {
        validateCoordinate(x, y);
    }

    /**
     * Checks whether another position is adjacent.
     */
    public boolean isAdjacentTo(Coordinate other) {
        validateOther(other);

        int dx = other.x - this.x;
        int dy = other.y - this.y;

        if (dx == 0 && dy == 0) {
            return false;
        }

        if (this.y % 2 != 0) {
            return switch (dy) {
                case -1 -> dx == 0 || dx == 1;
                case 0 -> dx == -1 || dx == 1;
                case 1 -> dx == 0 || dx == 1;
                default -> false;
            };
        } else {
            return switch (dy) {
                case -1 -> dx == -1 || dx == 0;
                case 0 -> dx == -1 || dx == 1;
                case 1 -> dx == -1 || dx == 0;
                default -> false;
            };
        }
    }


    public int distanceTo(Coordinate other) {
        validateOther(other);

        CubeCoordinate a = toCube();
        CubeCoordinate b = other.toCube();

        int dx = Math.abs(a.x() - b.x());
        int dy = Math.abs(a.y() - b.y());
        int dz = Math.abs(a.z() - b.z());

        return Math.max(dx, Math.max(dy, dz));
    }


    public Coordinate getNeighbor(Direction direction) {

        if (direction == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Direction cannot be null"
            );
        }

        return new Coordinate(
                x + direction.getDx(y),
                y + direction.getDy(y)
        );
    }


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

        if (x < 0 || y < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate cannot be negative"
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


    private record CubeCoordinate(
            int x,
            int y,
            int z
    ) {}
}