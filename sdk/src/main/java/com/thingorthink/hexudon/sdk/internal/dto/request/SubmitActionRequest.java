package com.thingorthink.hexudon.sdk.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * DTO request used to submit agent actions in official matches.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Unique identifier of the game
 * @param day Current game day
 * @param actions Nested list of action codes for each agent
 */
public record SubmitActionRequest(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("day") int day,
        @JsonProperty("actions") List<List<Integer>> actions
) {

    /**
     * Compact constructor ensuring immutability of actions list.
     */
    public SubmitActionRequest {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(actions, "actions must not be null");

        actions = actions.stream()
                .map(List::copyOf)
                .toList();
        actions = List.copyOf(actions);
    }
}
