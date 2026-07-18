package com.thingorthink.hexudon.sdk.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * DTO request used to submit agent actions in practice mode.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Practice game identifier
 * @param day Current practice day
 * @param actions Nested list of action codes for each agent
 */
public record PracticeSubmitRequest(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("day") int day,
        @JsonProperty("actions") List<List<Integer>> actions
) {

    /**
     * Compact constructor validating practice action submission request.
     */
    public PracticeSubmitRequest {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be blank");
        }
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }
        Objects.requireNonNull(actions, "actions must not be null");

        actions = actions.stream()
                .map(List::copyOf)
                .toList();
        actions = List.copyOf(actions);
    }
}
