package com.thingorthink.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents a move action for an agent.
 * <p>
 * A move action instructs the agent to move in one of the six
 * directions on the Odd-R hexagonal grid.
 */
public record MoveAction(Direction direction) implements GameAction {

    /**
     * Creates a move action.
     *
     * @param direction movement direction
     * @throws NullPointerException if direction is {@code null}
     */
    public MoveAction {
        Objects.requireNonNull(direction, "direction must not be null");
    }

    /**
     * Returns the integer protocol code defined by the server.
     *
     * @return protocol code in the range {@code 0..5}
     */
    @Override
    public int toProtocolCode() {
        return direction.getValue();
    }
}
