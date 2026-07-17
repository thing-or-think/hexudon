package com.naprock.hexudon.sdk.model.request;

import java.util.Objects;

/**
 * Request DTO for copying an opponent's practice progress.
 *
 * @param gameId the identifier of the current practice game
 * @param fromGameId the identifier of the source practice game
 * @param fromTeamId the identifier of the source team
 * @param uptoDay the last day to copy from the source game
 */
public record PracticeCopyRequest(
        String gameId,
        String fromGameId,
        String fromTeamId,
        int uptoDay
) {

    /**
     * Creates a new {@code PracticeCopyRequest}.
     *
     * @throws NullPointerException if any required identifier is {@code null}
     * @throws IllegalArgumentException if any identifier is blank or {@code uptoDay} is negative
     */
    public PracticeCopyRequest {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(fromGameId, "fromGameId must not be null");
        Objects.requireNonNull(fromTeamId, "fromTeamId must not be null");

        if (gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be blank");
        }

        if (fromGameId.isBlank()) {
            throw new IllegalArgumentException("fromGameId must not be blank");
        }

        if (fromTeamId.isBlank()) {
            throw new IllegalArgumentException("fromTeamId must not be blank");
        }

        if (uptoDay < 0) {
            throw new IllegalArgumentException("uptoDay must not be negative");
        }
    }
}