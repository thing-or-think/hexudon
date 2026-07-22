package com.naprock.hexudon.domain.model.geometry;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import static com.naprock.hexudon.domain.validation.DomainValidator.requirePositive;

/**
 * Represents the six movement directions on an Odd-R horizontal hex grid.
 */
public enum Direction {

    EAST,
    SOUTHEAST,
    SOUTHWEST,
    WEST,
    NORTHWEST,
    NORTHEAST;

    /**
     * Converts an API direction value to a Direction.
     *
     * <pre>
     * 0 -> NORTHWEST
     * 1 -> NORTHEAST
     * 2 -> EAST
     * 3 -> SOUTHEAST
     * 4 -> SOUTHWEST
     * 5 -> WEST
     * </pre>
     *
     * @param value API direction value
     * @return corresponding direction
     */
    public static Direction fromValue(int value) {
        return switch (value) {
            case 0 -> NORTHWEST;
            case 1 -> NORTHEAST;
            case 2 -> EAST;
            case 3 -> SOUTHEAST;
            case 4 -> SOUTHWEST;
            case 5 -> WEST;
            default -> throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("Invalid direction value: %d", value)
            );
        };
    }

    /**
     * Returns the column offset for this direction.
     *
     * @param row current row
     * @return column offset
     */
    public int getDx(int row) {
        requirePositive(row, "row");

        boolean oddRow = row % 2 != 0;

        return switch (this) {
            case EAST -> 1;

            case WEST -> -1;

            case SOUTHEAST -> oddRow ? 0 : 1;

            case SOUTHWEST -> oddRow ? -1 : 0;

            case NORTHWEST -> oddRow ? -1 : 0;

            case NORTHEAST -> oddRow ? 0 : 1;
        };
    }

    /**
     * Returns the row offset for this direction.
     *
     * @param row current row
     * @return row offset
     */
    public int getDy(int row) {
        requirePositive(row, "row");

        return switch (this) {
            case EAST, WEST -> 0;

            case SOUTHEAST, SOUTHWEST -> 1;

            case NORTHWEST, NORTHEAST -> -1;
        };
    }

    /**
     * Finds the direction corresponding to the given offsets.
     *
     * @param dx column offset
     * @param dy row offset
     * @param row current row
     * @return matching direction
     */
    public static Direction fromOffsets(int dx, int dy, int row) {
        requirePositive(row, "row");

        for (Direction direction : values()) {
            if (direction.getDx(row) == dx
                    && direction.getDy(row) == dy) {
                return direction;
            }
        }

        throw new GameRuleViolationException(
                ErrorCode.VALIDATION_ERROR,
                String.format(
                        "Invalid direction offsets: dx=%d, dy=%d, row=%d",
                        dx,
                        dy,
                        row
                )
        );
    }
}