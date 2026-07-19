package com.naprock.hexudon.application.dto.config;

import com.naprock.hexudon.application.dto.board.MapResponse;
import com.naprock.hexudon.application.dto.board.SpotResponse;

import java.util.List;
import java.util.Objects;

public record MatchConfigResponse(
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
) {

    public MatchConfigResponse {

        if (startsAt <= 0) {
            throw new IllegalArgumentException("startsAt must be positive");
        }

        daySeconds = List.copyOf(
                Objects.requireNonNull(daySeconds, "daySeconds must not be null")
        );

        daySteps = List.copyOf(
                Objects.requireNonNull(daySteps, "daySteps must not be null")
        );

        map = Objects.requireNonNull(map, "map must not be null");

        spots = List.copyOf(
                Objects.requireNonNull(spots, "spots must not be null")
        );

        agents = List.copyOf(
                Objects.requireNonNull(agents, "agents must not be null")
        );

        if (daySeconds.isEmpty()) {
            throw new IllegalArgumentException("daySeconds must not be empty");
        }

        if (daySteps.isEmpty()) {
            throw new IllegalArgumentException("daySteps must not be empty");
        }

        if (agents.isEmpty()) {
            throw new IllegalArgumentException("agents must not be empty");
        }

        if (daySeconds.size() != daySteps.size()) {
            throw new IllegalArgumentException(
                    "daySeconds and daySteps must have the same size"
            );
        }

        for (Double second : daySeconds) {
            if (second == null || second <= 0) {
                throw new IllegalArgumentException(
                        "Each value in daySeconds must be greater than 0"
                );
            }
        }

        for (Integer step : daySteps) {
            if (step == null || step <= 0) {
                throw new IllegalArgumentException(
                        "Each value in daySteps must be greater than 0"
                );
            }
        }

        for (Integer agent : agents) {
            if (agent == null || agent < 0) {
                throw new IllegalArgumentException(
                        "Each agent position must be non-negative"
                );
            }
        }

        if (fuelLimits <= 0) {
            throw new IllegalArgumentException("fuelLimits must be positive");
        }

        if (players <= 0) {
            throw new IllegalArgumentException("players must be positive");
        }

        if (busyThreshold <= 0) {
            throw new IllegalArgumentException(
                    "busyThreshold must be greater than 0"
            );
        }

        if (jammedThreshold <= busyThreshold) {
            throw new IllegalArgumentException(
                    "jammedThreshold must be greater than busyThreshold"
            );
        }
    }
}