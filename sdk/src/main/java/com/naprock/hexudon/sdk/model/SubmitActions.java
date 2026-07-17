package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a batch of movement actions submitted for a game day.
 *
 * <p>Each inner list contains the movement directions for a single agent,
 * ordered by execution sequence.
 *
 * @param day the game day to which the actions apply
 * @param actions the movement paths for each agent
 */
public record SubmitActions(
        int day,
        List<List<Direction>> actions
) {

    /**
     * Creates a new {@code SubmitActions}.
     *
     * @throws NullPointerException if {@code actions} is {@code null}
     * @throws IllegalArgumentException if {@code day} is negative
     */
    public SubmitActions {
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        Objects.requireNonNull(actions, "actions must not be null");

        actions = actions.stream()
                .map(path -> path == null
                        ? List.<Direction>of()
                        : List.copyOf(path))
                .toList();

        actions = List.copyOf(actions);
    }
}