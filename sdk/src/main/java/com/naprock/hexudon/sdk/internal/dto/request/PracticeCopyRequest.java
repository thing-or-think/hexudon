package com.naprock.hexudon.sdk.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO request used to copy a practice game progress.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Target practice game identifier
 * @param fromGameId Source practice game identifier
 * @param fromTeamId Source team identifier
 * @param uptoDay Maximum day to copy
 */
public record PracticeCopyRequest(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("from_game_id") String fromGameId,
        @JsonProperty("from_team_id") String fromTeamId,
        @JsonProperty("upto_day") int uptoDay
) {

    /**
     * Compact constructor validating practice copy request.
     */
    public PracticeCopyRequest {
        validateNotBlank(gameId, "gameId");
        validateNotBlank(fromGameId, "fromGameId");
        validateNotBlank(fromTeamId, "fromTeamId");
        if (uptoDay < 0) {
            throw new IllegalArgumentException("uptoDay must not be negative");
        }
    }

    private static void validateNotBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
