package com.naprock.hexudon.application.dto;

public record RankingResponse(
        int rank,
        String teamId,
        int uniqueUdon,
        int accumulatedUdon,
        int servings,
        long responseTime
) {
}