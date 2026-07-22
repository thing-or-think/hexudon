package com.naprock.hexudon.domain.model.movement;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.board.TerrainType;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;

/**
 * Immutable value object representing the movement cost of an agent.
 * Includes both fuel consumption and action steps required.
 */
public record MovementCost(
        int fuelNeeded,
        int stepsNeeded
) {

    public MovementCost {
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
    }

    public static MovementCost from(TerrainType terrainType) {
        return new MovementCost(
                terrainType.getFuelCost(),
                terrainType.getStepCost()
        );
    }

    public static MovementCost from(TrafficLevel trafficLevel) {
        return new MovementCost(
                TerrainType.ROAD.getFuelCost(),
                trafficLevel.cost()
        );
    }

    public static MovementCost forRoad(TrafficLevel trafficLevel) {
        return from(trafficLevel);
    }
}