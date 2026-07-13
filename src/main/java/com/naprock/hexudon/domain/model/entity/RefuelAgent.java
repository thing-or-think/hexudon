package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;

import java.util.Objects;

public class RefuelAgent extends Agent{

    public RefuelAgent(Coordinate coordinate) {
        super(coordinate);
    }

    public RefuelAgent(RefuelAgent other) {
        super(other);
    }

    @Override
    public Agent deepCopy() {
        return new RefuelAgent(this);
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

        consumeStep(stepCost);

        this.coordinate = destination;

        return MoveResult.success(
                coordinate,
                stepCost,
                0
        );
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
