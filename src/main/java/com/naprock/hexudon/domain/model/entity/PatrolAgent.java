package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
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
    public MoveResult executeAction(
            Action action,
            MatchState state,
            MatchConfig config) {

        validateNotNull(action, "action");
        validateNotNull(state, "state");
        validateNotNull(config, "config");

        if (action.getActionType() == ActionType.WAIT) {

            consumeStep(1);

            return MoveResult.success(
                    coordinate,
                    1,
                    0
            );
        }

        Coordinate destination = action.getTargetCoordinate();

        Cell cell = state.getCell(destination);

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

        int stepCost;
        int fuelCost;

        switch (cell.getTerrainType()) {

            case ROAD -> {
                stepCost = config.roadStepCost();
                fuelCost = config.roadFuelCost();
            }

            case PLAIN -> {
                stepCost = config.plainStepCost();
                fuelCost = config.plainFuelCost();
            }

            case MOUNTAIN -> {
                stepCost = config.mountainStepCost();
                fuelCost = config.mountainFuelCost();
            }

            default ->
                    throw new GameRuleViolationException(
                            ErrorCode.INVALID_TARGET_TERRAIN,
                            String.format(
                                    "Unsupported terrain type: %s.",
                                    cell.getTerrainType()
                            )
                    );
        }

        consumeStep(stepCost);
        consumeFuel(fuelCost);

        this.coordinate = destination;

        return MoveResult.success(
                coordinate,
                stepCost,
                fuelCost
        );
    }

    public void collectUdon(MatchState state,
                            Team team) {

        validateNotNull(state, "state");
        validateNotNull(team, "team");

        Spot targetSpot = null;

        for (Spot spot : state.getSpots()) {
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
