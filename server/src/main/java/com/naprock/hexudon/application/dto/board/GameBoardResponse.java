package com.naprock.hexudon.application.dto.board;

import com.naprock.hexudon.application.dto.config.MapResponse;

import java.util.List;

public record GameBoardResponse(
        String gameId,
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        MapResponse map,
        List<SpotResponse> spots,
        int fuelLimits,
        int players,
        double busyThreshold,
        double jammedThreshold
) {
}