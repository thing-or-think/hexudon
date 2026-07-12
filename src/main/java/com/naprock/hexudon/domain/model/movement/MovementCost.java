package com.naprock.hexudon.domain.model.movement;


import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Objects;

/**
 * Immutable value object representing the movement cost of an agent.
 * Includes both fuel consumption and action steps required.
 */
public class MovementCost {

    private final int fuelNeeded;
    private final int stepsNeeded;

    public MovementCost(int fuelNeeded, int stepsNeeded) {
        if (fuelNeeded < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Fuel needed must be greater than or equal to 0."
            );
        }

        if (stepsNeeded < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Steps needed must be greater than or equal to 0."
            );
        }

        this.fuelNeeded = fuelNeeded;
        this.stepsNeeded = stepsNeeded;
    }

    public int getFuelNeeded() {
        return fuelNeeded;
    }

    public int getStepsNeeded() {
        return stepsNeeded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovementCost that)) {
            return false;
        }
        return fuelNeeded == that.fuelNeeded
                && stepsNeeded == that.stepsNeeded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fuelNeeded, stepsNeeded);
    }

    @Override
    public String toString() {
        return "MovementCost{" +
                "fuelNeeded=" + fuelNeeded +
                ", stepsNeeded=" + stepsNeeded +
                '}';
    }
}
