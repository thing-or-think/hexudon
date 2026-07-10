package com.naprock.hexudon.domain.model.valueobject;


import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Objects;

/**
 * Value Object representing the result of an Agent movement action.
 *
 * Contains execution status, final position,
 * movement cost, fuel consumption and message.
 */
public final class MoveResult {

    private final boolean success;
    private final Coordinate targetCoordinate;
    private final int stepCost;
    private final int fuelCost;
    private final String message;


    /**
     * Full constructor.
     *
     * @param success execution status
     * @param targetCoordinate final coordinate after action
     * @param stepCost movement cost
     * @param fuelCost fuel consumption
     * @param message result description
     */
    public MoveResult(
            boolean success,
            Coordinate targetCoordinate,
            int stepCost,
            int fuelCost,
            String message
    ) {

        validate(
                targetCoordinate,
                stepCost,
                fuelCost
        );

        this.success = success;
        this.targetCoordinate = targetCoordinate;
        this.stepCost = stepCost;
        this.fuelCost = fuelCost;
        this.message = message == null ? "" : message;
    }


    public boolean isSuccess() {
        return success;
    }


    public Coordinate getTargetCoordinate() {
        return targetCoordinate;
    }


    public int getStepCost() {
        return stepCost;
    }


    public int getFuelCost() {
        return fuelCost;
    }


    public String getMessage() {
        return message;
    }


    /**
     * Factory method for successful movement.
     */
    public static MoveResult success(
            Coordinate target,
            int stepCost,
            int fuelCost
    ) {

        return new MoveResult(
                true,
                target,
                stepCost,
                fuelCost,
                ""
        );
    }


    /**
     * Factory method for failed movement.
     */
    public static MoveResult failed(
            Coordinate current,
            String message
    ) {

        return new MoveResult(
                false,
                current,
                0,
                0,
                message
        );
    }


    /**
     * Factory method when Agent chooses not to move.
     */
    public static MoveResult waitResult(
            Coordinate current
    ) {

        return new MoveResult(
                true,
                current,
                0,
                0,
                "Agent waits"
        );
    }


    private static void validate(
            Coordinate targetCoordinate,
            int stepCost,
            int fuelCost
    ) {

        if (targetCoordinate == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Target coordinate cannot be null"
            );
        }


        if (stepCost < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Step cost cannot be negative: " + stepCost
            );
        }


        if (fuelCost < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Fuel cost cannot be negative: " + fuelCost
            );
        }
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MoveResult other)) {
            return false;
        }

        return success == other.success
                && stepCost == other.stepCost
                && fuelCost == other.fuelCost
                && Objects.equals(
                targetCoordinate,
                other.targetCoordinate
        )
                && Objects.equals(
                message,
                other.message
        );
    }


    @Override
    public int hashCode() {

        return Objects.hash(
                success,
                targetCoordinate,
                stepCost,
                fuelCost,
                message
        );
    }


    @Override
    public String toString() {

        return "MoveResult{" +
                "success=" + success +
                ", targetCoordinate=" + targetCoordinate +
                ", stepCost=" + stepCost +
                ", fuelCost=" + fuelCost +
                ", message='" + message + '\'' +
                '}';
    }
}