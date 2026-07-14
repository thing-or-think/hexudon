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

/**
 * Abstract base class for all agents.
 */
public abstract class Agent {

    private static int nextId = 1;

    private final AgentType agentType;
    private final String id;
    protected Coordinate position;
    protected int fuel;
    protected int remainingSteps;
    private List<Action> actions;

    protected Agent(
            Coordinate position,
            AgentType agentType
    ) {

        validateNotNull(position, "position");
        validateNotNull(agentType, "agenType");
        this.agentType = agentType;
        this.id = "A" + nextId++;
        this.position = position;
        this.fuel = 0;
        this.remainingSteps = 0;
        this.actions = new ArrayList<>();
    }

    public boolean isEmptyAction() {
        return actions.isEmpty();
    }

    public void prepareNewTurn() {
    }

    public CollectResult collectUdon(
            String teamName,
            Map<Coordinate, Spot> spots) {
        return CollectResult.failed(id, position);
    }

    protected Action consumeNextAction() {
        if (actions == null || actions.isEmpty()) {
            return Action.stay(position);
        }

        return actions.removeFirst();
    }

    public int getFuel() {
        return fuel;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public String getId() {
        return id;
    }

    public Coordinate getPosition() {
        return position;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    /**
     * Replace action queue.
     */
    public void setActions(List<Action> actions) {

        if (actions == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Actions cannot be null"
            );
        }

        this.actions = new ArrayList<>(actions);
    }

    /**
     * Set current fuel.
     */
    public void setFuel(int fuel) {

        if (fuel < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Fuel cannot be negative"
            );
        }

        this.fuel = fuel;
    }

    public void refuel(int maxFuel) {
        if (maxFuel <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Max fuel must be greater than zero"
            );
        }

        this.fuel = maxFuel;
    }

    /**
     * Reset resources at beginning of a turn.
     */
    public void resetSteps(
            int maxSteps
    ) {

        if (maxSteps <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Max steps must be greater than zero"
            );
        }

        this.remainingSteps = maxSteps;
    }

    /**
     * Consume movement steps.
     */
    protected boolean consumeStep(int cost) {

        if (cost < 0 || cost > remainingSteps) {
            return false;
        }

        remainingSteps -= cost;
        return true;
    }

    /**
     * Consume fuel.
     */
    protected boolean consumeFuel(int cost) {

        if (cost < 0 || cost > fuel) {
            return false;
        }

        fuel -= cost;

        return false;
    }

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

        Coordinate destination = action.targetCoordinate();
        Cell cell = cells.get(destination);

        if (cell == null
                || !position.isAdjacentTo(destination)
                || cell.isWalkable()) {
            consumeStep(1);
            return MoveResult.failed(position);
        }

        MovementCost movementCost = movementCosts.get(destination);
        if (!consumeStep(movementCost.getStepsNeeded())) {
            return MoveResult.failed(position);
        }

        position = destination;

        return MoveResult.success(position);
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

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Agent other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @Override
    public String toString() {

        return getClass().getSimpleName() +
                "{" +
                "id='" + id + '\'' +
                ", position=" + position +
                ", fuel=" + fuel +
                ", remainingSteps=" + remainingSteps +
                '}';
    }

}
