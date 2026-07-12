package com.naprock.hexudon.application.dto;

public record TeamScoreResponse(
        String teamId,
        int uniqueUdonTypesCount,
        int accumulatedDailyUdon,
        int totalServings,
        long totalResponseTimeMs
) {
}