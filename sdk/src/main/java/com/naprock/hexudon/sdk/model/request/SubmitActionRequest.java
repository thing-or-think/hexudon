package com.naprock.hexudon.sdk.model.request;

import java.util.List;
import java.util.Objects;

/**
 * Request DTO representing the queued actions of all agents for a specific game day.
 *
 * @param day     current game day
 * @param actions queued movement actions for each agent
 */
public record SubmitActionRequest(
        int day,
        List<List<Integer>> actions
) {

    public SubmitActionRequest {
        Objects.requireNonNull(actions, "actions must not be null");

        actions = List.copyOf(
                actions.stream()
                        .map(list -> List.copyOf(
                                Objects.requireNonNull(list, "agent actions must not be null")
                        ))
                        .toList()
        );
    }
}