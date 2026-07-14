package com.naprock.hexudon.domain.model.movement;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.Objects;

/**
 * Value Object representing the result of an Agent movement action.
 *
 * Contains movement status, final position and execution message.
 */
public record MoveResult(
        boolean success,
        Coordinate position
) {

    public MoveResult {
        validate(position);
    }


    /**
     * Creates a successful movement result.
     */
    public static MoveResult success(Coordinate position) {
        return new MoveResult(
                true,
                position
        );
    }


    /**
     * Creates a failed movement result.
     */
    public static MoveResult failed(Coordinate currentPosition) {
        return new MoveResult(
                false,
                currentPosition
        );
    }


    /**
     * Creates a result when Agent decides not to move.
     */
    public static MoveResult waitResult(Coordinate currentPosition) {
        return new MoveResult(
                true,
                currentPosition
        );
    }


    private static void validate(Coordinate position) {

        if (position == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate cannot be null"
            );
        }
    }
}