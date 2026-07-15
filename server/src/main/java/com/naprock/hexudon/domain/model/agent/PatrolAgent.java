package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.Spot;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.team.CollectResult;

import java.util.*;

public class PatrolAgent extends Agent {

    private final List<Coordinate> visitedSpotsToday = new ArrayList<>();

    public PatrolAgent(Coordinate coordinate) {
        super(coordinate, AgentType.PATROL);
    }

    @Override
    public void prepareNewTurn() {
        visitedSpotsToday.clear();
    }

    @Override
    public MoveResult executeAction(
            Map<Coordinate, Cell> cells,
            Map<Coordinate, MovementCost> movementCosts) {

        validateNotNull(cells, "cells");
        validateNotNull(movementCosts, "movementCosts");

        Action action = consumeNextAction();

        if (action == null || action.actionType() == ActionType.WAIT) {
            consumeStep(1);
            return MoveResult.success(position);
        }

        Coordinate destination = position.getNeighbor(action.direction());
        Cell cell = cells.get(destination);

        if (cell == null || !cell.isWalkable()) {
            consumeStep(1);
            return MoveResult.failed(position);
        }

        MovementCost movementCost = movementCosts.get(destination);
        if (!consumeFuel(movementCost.fuelNeeded())) {
            return MoveResult.failed(position);
        }

        consumeStep(movementCost.stepsNeeded());

        position = destination;

        return MoveResult.success(position);
    }

    @Override
    public CollectResult collectUdon(
            int teamId,
            Map<Coordinate, Spot> spots) {

        validateNotNull(spots, "spots");

        Spot spot = spots.get(position);
        if (spot == null || visitedSpotsToday.contains(spot.getCoordinate()) || spot.getStock(teamId) <= 0) {
            return CollectResult.failed(teamId, position);
        }

        spot.decrementStock(teamId);

        visitedSpotsToday.add(spot.getCoordinate());
        return CollectResult.success(teamId, position, spot.getType());
    }

    private void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }
}
