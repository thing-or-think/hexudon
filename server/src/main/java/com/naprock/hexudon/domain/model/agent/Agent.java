package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.validation.DomainValidator;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public abstract class Agent {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final AgentType agentType;

    protected Coordinate position;
    protected int remainingSteps;

    protected Agent(
            Coordinate position,
            AgentType agentType
    ) {
        DomainValidator.requireNonNull(position, "position");
        DomainValidator.requireNonNull(agentType, "agentType");

        this.id = NEXT_ID.getAndIncrement();
        this.agentType = agentType;
        this.position = position;
    }

    /**
     * Called at the beginning of every turn.
     */
    public void prepareNewTurn(int steps) {
        resetSteps(steps);
    }

    /**
     * Whether this agent has enough resources to move.
     */
    public boolean canMove(MovementCost cost) {
        DomainValidator.requireNonNull(cost, "cost");

        return remainingSteps >= cost.stepsNeeded();
    }

    /**
     * Apply a successful movement.
     * Validation of destination is responsibility of MovementService.
     */
    public void moveTo(
            Coordinate destination,
            MovementCost cost
    ) {
        DomainValidator.requireNonNull(destination, "destination");
        DomainValidator.requireNonNull(cost, "cost");

        requireTrue(canMove(cost), "Agent does not have enough fuel or steps.");

        remainingSteps -= cost.stepsNeeded();
        position = destination;
    }

    /**
     * Consume one step without moving.
     */
    public void waitAction() {
        requirePositive(remainingSteps, "remainingSteps");
        remainingSteps--;
    }

    public abstract Agent copy(int steps);


    protected void resetSteps(int steps) {
        requireNonNegative(steps, "steps");
        remainingSteps = steps;
    }

    public Coordinate getPosition() {
        return position;
    }


    public int getRemainingSteps() {
        return remainingSteps;
    }

    public boolean hasRemainingSteps(int steps) {
        return this.remainingSteps == steps;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public int getId() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Agent other)) {
            return false;
        }

        return id == other.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "%s{id=%d, position=%s, remainingSteps=%d}"
                .formatted(
                        getClass().getSimpleName(),
                        id,
                        position,
                        remainingSteps
                );
    }
}