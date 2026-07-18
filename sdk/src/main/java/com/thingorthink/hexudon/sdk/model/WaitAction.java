package com.thingorthink.hexudon.sdk.model;

/**
 * Represents a wait action for an agent.
 * <p>
 * A wait action keeps the agent in its current position for a
 * specified number of steps.
 */
public record WaitAction(int steps) implements GameAction {

    /**
     * Creates a wait action.
     *
     * @throws IllegalArgumentException if {@code steps <= 0}
     */
    public WaitAction {
        if (steps <= 0) {
            throw new IllegalArgumentException("steps must be greater than 0");
        }
    }

    /**
     * Returns the protocol code defined by the server.
     * <p>
     * Waiting is represented as a negative integer equal to the
     * number of waiting steps.
     *
     * @return {@code -steps}
     */
    @Override
    public int toProtocolCode() {
        return -steps;
    }
}
