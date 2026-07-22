package com.naprock.hexudon.application.dto.game;

import com.naprock.hexudon.application.dto.config.MapResponse;

public record GameSummaryResponse(
        String gameId,
        long startsAt,
        int players,
        int fuelLimits,
        double agentSelectionTimeLimit,
        double busyThreshold,
        double jammedThreshold,
        MapResponse map,
        int totalDays
) {
}
