package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record MatchConfig(

        long startsAt,
        List<Integer> daySeconds,
        List<Integer> daySteps,
        MapConfig map,
        List<SpotConfig> spots,
        List<Integer> agents,
        int fuelLimits,
        int players,
        double busyThreshold,
        double jammedThreshold

) {

    public MatchConfig {

        requirePositive(startsAt, "startsAt");

        requireNonNull(daySeconds, "daySeconds");
        requireNonNull(daySteps, "daySteps");
        requireNonNull(map, "map");
        requireNonNull(spots, "spots");
        requireNonNull(agents, "agents");

        requirePositive(fuelLimits, "fuelLimits");
        requirePositive(players, "players");

        requireTrue(
                !daySeconds.isEmpty(),
                "daySeconds must not be empty."
        );

        requireTrue(
                !daySteps.isEmpty(),
                "daySteps must not be empty."
        );

        requireTrue(
                !spots.isEmpty(),
                "spots must not be empty."
        );

        requireTrue(
                !agents.isEmpty(),
                "agents must not be empty."
        );

        requireTrue(
                daySeconds.size() == daySteps.size(),
                "daySeconds and daySteps must have the same size."
        );

        requireTrue(
                busyThreshold > 0,
                "busyThreshold must be greater than 0."
        );

        requireTrue(
                jammedThreshold > busyThreshold,
                "jammedThreshold must be greater than busyThreshold."
        );

        daySeconds = List.copyOf(daySeconds);
        daySteps = List.copyOf(daySteps);
        spots = List.copyOf(spots);
        agents = List.copyOf(agents);
    }

    public MatchConfig withStartsAt(long startsAt) {
        return new MatchConfig(
                startsAt,
                daySeconds,
                daySteps,
                map,
                spots,
                agents,
                fuelLimits,
                players,
                busyThreshold,
                jammedThreshold
        );
    }
}