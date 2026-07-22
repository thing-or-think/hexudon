package com.naprock.hexudon.application.dto.config;

import com.naprock.hexudon.application.dto.board.SpotResponse;

import java.util.List;

public record GameConfigResponse(

        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,

        MapResponse map,

        List<SpotResponse> spots,

        List<Integer> agents,

        int fuelLimits,
        int players,

        double busyThreshold,
        double jammedThreshold

) {}