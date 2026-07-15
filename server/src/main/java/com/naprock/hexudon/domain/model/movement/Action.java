package com.naprock.hexudon.domain.model.movement;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Direction;

import java.util.ArrayList;
import java.util.List;

public record Action(
        ActionType actionType,
        Direction direction
) {

    public Action {
        validate(actionType, direction);
    }

    public static List<Action> fromApiValue(int value) {

        if (value >= 0) {
            return List.of(move(Direction.fromValue(value)));
        }

        List<Action> actions = new ArrayList<>(-value);

        for (int i = 0; i < -value; i++) {
            actions.add(stay());
        }

        return actions;
    }

    /**
     * Creates MOVE action.
     */
    public static Action move(Direction direction) {
        return new Action(
                ActionType.MOVE,
                direction
        );
    }

    /**
     * Creates WAIT action.
     */
    public static Action stay() {
        return new Action(
                ActionType.WAIT,
                null
        );
    }

    private static void validate(
            ActionType actionType,
            Direction direction
    ) {

        if (actionType == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Action type cannot be null"
            );
        }

        if (actionType == ActionType.MOVE
                && direction == null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "MOVE action requires direction"
            );
        }

        if (actionType == ActionType.WAIT
                && direction != null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "WAIT action cannot have direction"
            );
        }
    }
}