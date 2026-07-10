package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MoveResult;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.model.aggregate.MatchState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all agents.
 */
public abstract class Agent {

    private static int nextId = 1;

    private final String id;

    protected Coordinate coordinate;

    protected int fuel;

    protected int remainingSteps;

    private List<Action> actions;

    protected Agent(
            Coordinate coordinate
    ) {

        validateNotNull(coordinate, "coordinate");
        this.id = "A" + nextId++;
        this.coordinate = coordinate;
        this.fuel = 0;
        this.remainingSteps = 0;
        this.actions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }


    public Coordinate getCoordinate() {
        return coordinate;
    }


    public int getFuel() {
        return fuel;
    }


    public int getRemainingSteps() {
        return remainingSteps;
    }

    /**
     * Returns an immutable view of actions.
     */
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
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

    /**
     * Reset resources at beginning of a day.
     */
    public void resetTurnResources(
            int maxFuel,
            int maxSteps
    ) {

        if (maxFuel <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Max fuel must be greater than zero"
            );
        }

        if (maxSteps <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Max steps must be greater than zero"
            );
        }

        this.fuel = maxFuel;
        this.remainingSteps = maxSteps;
        this.actions.clear();
    }

    /**
     * Consume movement steps.
     */
    protected void consumeStep(int cost) {

        if (cost < 0 || cost > remainingSteps) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Not enough remaining steps"
            );
        }

        remainingSteps -= cost;
    }

    /**
     * Consume fuel.
     */
    protected void consumeFuel(int cost) {

        if (cost < 0 || cost > fuel) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Not enough fuel"
            );
        }

        fuel -= cost;
    }

    /**
     * Update current position.
     * Only subclasses may move the agent.
     */
    protected void moveTo(Coordinate coordinate) {

        Objects.requireNonNull(coordinate);

        this.coordinate = coordinate;
    }

    /**
     * Execute one action.
     */
    public abstract MoveResult executeAction(
            Action action,
            MatchState state,
            MatchConfig config
    );

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
                ", coordinate=" + coordinate +
                ", fuel=" + fuel +
                ", remainingSteps=" + remainingSteps +
                '}';
    }

}
