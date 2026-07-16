package com.naprock.hexudon.sdk.model.request;

import java.util.List;
import java.util.Objects;

public record PracticeSubmitRequest(
        String gameId,
        int day,
        List<List<Integer>> actions
) {

    public PracticeSubmitRequest {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(actions, "actions must not be null");

        if (gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be blank");
        }

        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        actions = actions.stream()
                .map(path -> {
                    Objects.requireNonNull(path, "action list must not be null");
                    return List.copyOf(path);
                })
                .toList();

        actions = List.copyOf(actions);
    }
}