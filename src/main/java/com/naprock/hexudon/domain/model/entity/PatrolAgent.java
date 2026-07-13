package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PatrolAgent extends Agent {

    private final List<Coordinate> visitedSpotsToday = new ArrayList<>();

    public PatrolAgent(Coordinate coordinate) {
        super(coordinate);
    }

    public PatrolAgent(PatrolAgent other) {
        super(other);
    }

    public List<Coordinate> getVisitedSpotsToday() {
        return Collections.unmodifiableList(visitedSpotsToday);
    }

    public void clearVisitedSpotsToday() {
        visitedSpotsToday.clear();
    }

    public boolean hasVisitedSpotToday(Coordinate spotCoord) {
        validateNotNull(spotCoord, "spotCoord");
        return visitedSpotsToday.contains(spotCoord);
    }

    @Override
    public Agent deepCopy() {
        return new PatrolAgent(this);
    }

    @Override
    public MoveResult executeAction(
            Action action,
            GameMap gameMap) {

        validateNotNull(action, "action");
        validateNotNull(gameMap, "gameMap");

        if (action.getActionType() == ActionType.WAIT) {

            consumeStep(1);

            return MoveResult.success(
                    coordinate,
                    1,
                    0
            );
        }

        Coordinate destination = action.getTargetCoordinate();

        Cell cell = gameMap.getCell(destination);

        if (cell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "Destination terrain is not walkable."
            );
        }

        if (!coordinate.isAdjacentTo(destination)) {
            throw new GameRuleViolationException(
                    ErrorCode.CELL_OUT_OF_BOUNDS,
                    "Destination %s is not adjacent to current position %s."
            );
        }

        if (cell.getTerrainType() == TerrainType.POND) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "Cannot move onto a pond cell."
            );
        }

        MovementCost movementCost = gameMap.getMovementCosts().get(cell.getCoordinate());

        int stepCost = movementCost.getStepsNeeded();
        int fuelCost = movementCost.getFuelNeeded();

        consumeStep(stepCost);
        consumeFuel(fuelCost);

        this.coordinate = destination;

        return MoveResult.success(
                coordinate,
                stepCost,
                fuelCost
        );
    }

    public void collectUdon(
            int turn,
            GameMap gameMap,
            Team team) {

        validateNotNull(gameMap, "state");
        validateNotNull(team, "team");

        Spot targetSpot = null;

        for (Spot spot : gameMap.getSpots()) {
            if (spot.getCoordinate().equals(coordinate)) {
                targetSpot = spot;
                break;
            }
        }

        if (targetSpot == null) {
            return;
        }

        if (hasVisitedSpotToday(targetSpot.getCoordinate())) {
            return;
        }

        int stock = targetSpot.getUdonStock(team.getTeamName());

        if (stock <= 0) {
            return;
        }

        targetSpot.decrementUdonStock(team.getTeamName());

        team.addCollectedUdon(1);

        visitedSpotsToday.add(targetSpot.getCoordinate());
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
