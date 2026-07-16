package com.naprock.hexudon.sdk.model.response;

import java.util.Objects;

/**
 * Response DTO containing the score information of a team.
 *
 * @param teamId the team identifier
 * @param score the current accumulated score
 * @param harvestCount the number of harvested udon bowls
 */
public record TeamScoreResponse(
        String teamId,
        int score,
        int harvestCount
) {

    /**
     * Creates a new {@code TeamScoreResponse}.
     *
     * @throws NullPointerException if {@code teamId} is {@code null}
     * @throws IllegalArgumentException if {@code teamId} is blank or any numeric value is negative
     */
    public TeamScoreResponse {
        Objects.requireNonNull(teamId, "teamId must not be null");

        if (teamId.isBlank()) {
            throw new IllegalArgumentException("teamId must not be blank");
        }

        if (score < 0) {
            throw new IllegalArgumentException("score must not be negative");
        }

        if (harvestCount < 0) {
            throw new IllegalArgumentException("harvestCount must not be negative");
        }
    }
}