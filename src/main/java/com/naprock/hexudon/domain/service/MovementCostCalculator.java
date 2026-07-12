package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.HashMap;
import java.util.Map;

public class MovementCostCalculator {

    public MovementCostCalculator() {
    }

    public Map<Coordinate, MovementCost> calculate(
            Map<Coordinate, Cell> cells,
            Map<Coordinate, TrafficFlow> flows,
            MatchConfig config
    ) {
        Map<Coordinate, MovementCost> costs = new HashMap<>();
        for (TrafficFlow flow : flows.values()) {
            Coordinate coordinate = flow.getCoordinate();
            costs.put(
                    coordinate,
                    calculate(
                            cells.get(coordinate).getTerrainType(),
                            flow.getTrafficLevel(),
                            config
                    )
                    );
        }

        return costs;
    }

    public MovementCost calculate(
            TerrainType terrain,
            TrafficLevel level,
            MatchConfig config
    ) {
        int fuelCost = switch (terrain) {
            case ROAD -> config.roadFuelCost();
            case PLAIN -> config.plainFuelCost();
            case MOUNTAIN -> config.mountainFuelCost();
            default -> throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Unknown terrain: " + terrain
            );
        };

        int stepCost = switch (terrain) {
            case ROAD -> {
                if (level == null) {
                    throw new GameRuleViolationException(
                            ErrorCode.VALIDATION_ERROR,
                            "Road requires traffic level"
                    );
                }

                yield switch (level) {
                    case NORMAL -> config.roadNormalStepCost();
                    case BUSY -> config.roadBusyStepCost();
                    case CONGESTED -> config.roadCongestedStepCost();
                };
            }

            case PLAIN -> config.plainStepCost();

            case MOUNTAIN -> config.mountainStepCost();

            default -> throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Unknown terrain: " + terrain
            );
        };

        return new MovementCost(fuelCost, stepCost);
    }
}
