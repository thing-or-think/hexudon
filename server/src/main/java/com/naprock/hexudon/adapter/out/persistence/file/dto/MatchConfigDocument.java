package com.naprock.hexudon.adapter.out.persistence.file.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchConfigDocument(

        String gameId,
        long startsAt,

        List<Double> daySeconds,
        List<Integer> daySteps,

        BoardConfigDocument map,

        List<Integer> agents,

        int fuelLimits,
        int players,

        double busyThreshold,
        double jammedThreshold,
        double agentSelectionTimeLimit

) {
}