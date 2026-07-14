package com.naprock.hexudon.application.dto.team;

import java.util.Objects;

public record TeamScoreResponse(
        String teamName,
        int uniqueUdonTypeCount,
        int totalDailyUdon,
        int totalUdonServings,
        long totalResponseTimeMillis
) {

    public TeamScoreResponse {
        Objects.requireNonNull(teamName, "teamName must not be null");

        if (teamName.isBlank()) {
            throw new IllegalArgumentException("teamName must not be blank");
        }

        if (uniqueUdonTypeCount < 0) {
            throw new IllegalArgumentException(
                    "uniqueUdonTypeCount must not be negative"
            );
        }

        if (totalDailyUdon < 0) {
            throw new IllegalArgumentException(
                    "totalDailyUdon must not be negative"
            );
        }

        if (totalUdonServings < 0) {
            throw new IllegalArgumentException(
                    "totalUdonServings must not be negative"
            );
        }

        if (totalResponseTimeMillis < 0) {
            throw new IllegalArgumentException(
                    "totalResponseTimeMillis must not be negative"
            );
        }
    }
}