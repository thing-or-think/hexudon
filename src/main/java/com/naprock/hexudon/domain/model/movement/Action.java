package com.naprock.hexudon.domain.model.movement;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

public record Action(
        ActionType actionType,
        Coordinate targetCoordinate
) {

    public Action {
        validate(actionType, targetCoordinate);
    }

    /**
     * Creates MOVE action.
     */
    public static Action move(Coordinate targetCoordinate) {
        return new Action(
                ActionType.MOVE,
                targetCoordinate
        );
    }

    /**
     * Creates STAY action.
     */
    public static Action stay(Coordinate targetCoordinate) {
        return new Action(
                ActionType.WAIT,
                targetCoordinate
        );
    }

    private static void validate(
            ActionType actionType,
            Coordinate targetCoordinate
    ) {

        if (actionType == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Action type cannot be null"
            );
        }

        if (actionType == ActionType.MOVE
                && targetCoordinate == null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "MOVE action requires target position"
            );
        }

        if (actionType == ActionType.WAIT
                && targetCoordinate != null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "WAIT action cannot have target position"
            );
        }
    }
}