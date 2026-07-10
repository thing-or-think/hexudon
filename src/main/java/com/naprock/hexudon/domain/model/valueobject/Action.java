package com.naprock.hexudon.domain.model.valueobject;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.valueobject.ActionType;

import java.util.Objects;

/**
 * Value Object representing an Agent action command.
 *
 * An Action describes one step in a daily action plan:
 * MOVE to a coordinate or WAIT.
 */
public class Action {

    private final int order;
    private final ActionType actionType;
    private final Coordinate targetCoordinate;
    private final long timestamp;

    /**
     * Creates an immutable action command.
     *
     * @param order execution order (1-indexed)
     * @param actionType action type
     * @param targetCoordinate destination coordinate for MOVE action
     * @param timestamp creation timestamp
     */
    public Action(
            int order,
            ActionType actionType,
            Coordinate targetCoordinate,
            long timestamp
    ) {

        validate(
                order,
                actionType,
                targetCoordinate,
                timestamp
        );

        this.order = order;
        this.actionType = actionType;
        this.targetCoordinate = targetCoordinate;
        this.timestamp = timestamp;
    }

    public int getOrder() {
        return order;
    }


    public ActionType getActionType() {
        return actionType;
    }


    public Coordinate getTargetCoordinate() {
        return targetCoordinate;
    }


    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Creates MOVE action.
     */
    public static Action move(
            int order,
            Coordinate targetCoordinate,
            long timestamp
    ) {

        return new Action(
                order,
                ActionType.MOVE,
                targetCoordinate,
                timestamp
        );
    }

    /**
     * Creates WAIT action.
     */
    public static Action wait(
            int order,
            long timestamp
    ) {

        return new Action(
                order,
                ActionType.WAIT,
                null,
                timestamp
        );
    }

    private static void validate(
            int order,
            ActionType actionType,
            Coordinate targetCoordinate,
            long timestamp
    ) {

        if (order <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Action order must be greater than zero: " + order
            );
        }


        if (actionType == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Action type cannot be null"
            );
        }


        if (timestamp <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Timestamp must be greater than zero: " + timestamp
            );
        }


        if (actionType == ActionType.MOVE
                && targetCoordinate == null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "MOVE action requires target coordinate"
            );
        }


        if (actionType == ActionType.WAIT
                && targetCoordinate != null) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "WAIT action cannot have target coordinate"
            );
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }


        if (!(obj instanceof Action other)) {
            return false;
        }


        return order == other.order
                && timestamp == other.timestamp
                && actionType == other.actionType
                && Objects.equals(
                targetCoordinate,
                other.targetCoordinate
        );
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                order,
                actionType,
                targetCoordinate,
                timestamp
        );
    }


    @Override
    public String toString() {

        return "Action{" +
                "order=" + order +
                ", actionType=" + actionType +
                ", targetCoordinate=" + targetCoordinate +
                ", timestamp=" + timestamp +
                '}';
    }
}
