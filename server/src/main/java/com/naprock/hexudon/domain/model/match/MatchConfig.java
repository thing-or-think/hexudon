package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.model.board.BoardConfig;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

/**
 * Immutable configuration for a game match.
 *
 * <p>This record contains all static configuration required to initialize and
 * run a HexUdon match.</p>
 */
public record MatchConfig(
        String gameId,
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        BoardConfig map,
        List<Integer> agents,
        int fuelLimits,
        int players,
        double busyThreshold,
        double jammedThreshold,
        double agentSelectionTimeLimit
) {

    public MatchConfig {
        requireNotBlank(gameId, "gameId");
        requirePositive(startsAt, "startsAt");

        requireNonNull(daySeconds, "daySeconds");
        requireNonNull(daySteps, "daySteps");
        requireNonNull(map, "map");
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

        requireTrue(
                agentSelectionTimeLimit > 0,
                "agentSelectionTimeLimit must be greater than 0."
        );

        int maxPosition = map.width() * map.height();

        requireTrue(
                agents.stream().allMatch(position -> position >= 0 && position < maxPosition),
                "agent position out of map bounds."
        );

        requireTrue(
                agents.stream().distinct().count() == agents.size(),
                "duplicate agent positions."
        );

        daySeconds = List.copyOf(daySeconds);
        daySteps = List.copyOf(daySteps);
        agents = List.copyOf(agents);
    }

    /**
     * Returns a copy of this configuration with a different start time.
     */
    public MatchConfig withStartsAt(long startsAt) {
        return new MatchConfig(
                gameId,
                startsAt,
                daySeconds,
                daySteps,
                map,
                agents,
                fuelLimits,
                players,
                busyThreshold,
                jammedThreshold,
                agentSelectionTimeLimit
        );
    }
}