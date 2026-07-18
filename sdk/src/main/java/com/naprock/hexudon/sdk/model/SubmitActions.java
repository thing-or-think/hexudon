package com.naprock.hexudon.sdk.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the actions submitted for all agents on a game day.
 *
 * @param day the game day
 * @param actions the action sequences of all agents
 */
public record SubmitActions(
        int day,
        List<List<GameAction>> actions
) {

    /**
     * Creates a new {@code SubmitActions}.
     *
     * @throws IllegalArgumentException if {@code day < 0}
     * @throws NullPointerException if {@code actions} or any inner list is {@code null}
     */
    public SubmitActions {
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        Objects.requireNonNull(actions, "actions must not be null");

        actions = List.copyOf(
                actions.stream()
                        .map(path -> List.copyOf(
                                Objects.requireNonNull(path, "agent actions must not be null")))
                        .toList()
        );
    }
}
