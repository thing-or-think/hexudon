package com.naprock.hexudon.sdk.internal.mapper;

import com.naprock.hexudon.sdk.internal.dto.response.*;
import com.naprock.hexudon.sdk.model.*;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for mapping match state data.
 *
 * <p>Visibility: package-private.</p>
 */
public final class MatchStateMapper {

    private MatchStateMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }


    /**
     * Converts MatchStateResponse DTO into MatchState domain model.
     *
     * @param dto match state response
     * @param mapWidth board width
     * @return match state domain model
     */
    public static MatchState toDomain(
            MatchStateResponse dto,
            int mapWidth
    ) {
        Objects.requireNonNull(
                dto,
                "Match state response must not be null"
        );

        if (mapWidth <= 0) {
            throw new IllegalArgumentException(
                    "Map width must be positive"
            );
        }


        List<Agent> agents = new java.util.ArrayList<>();
        for (int i = 0; i < dto.agents().size(); i++) {
            agents.add(toAgent(i, dto.agents().get(i), mapWidth));
        }


        List<Agent> others = new java.util.ArrayList<>();
        for (int i = 0; i < dto.others().size(); i++) {
            others.add(toAgent(i, dto.others().get(i), mapWidth));
        }


        List<Traffic> traffics =
                dto.traffics()
                        .stream()
                        .map(traffic -> {

                            Coordinate coordinate =
                                    new Coordinate(
                                            traffic.pos(),
                                            mapWidth
                                    );

                            TrafficLevel level =
                                    TrafficLevel.fromValue(
                                            traffic.status()
                                    );

                            return new Traffic(
                                    coordinate,
                                    level
                            );
                        })
                        .toList();


        return new MatchState(
                dto.endsAt(),
                dto.day(),
                agents,
                others,
                traffics,
                dto.status()
        );
    }


    /**
     * Converts TeamResponse DTO into Team domain model.
     *
     * @param dto team response
     * @param mapWidth board width
     * @return team domain model
     */
    public static Team toTeam(
            TeamResponse dto,
            int mapWidth
    ) {
        Objects.requireNonNull(
                dto,
                "Team response must not be null"
        );

        List<Agent> agents = new java.util.ArrayList<>();
        for (int i = 0; i < dto.agents().size(); i++) {
            agents.add(toAgent(i, dto.agents().get(i), mapWidth));
        }

        return new Team(
                dto.id(),
                agents
        );
    }


    private static Agent toAgent(
            int agentId,
            AgentResponse response,
            int mapWidth
    ) {
        Coordinate coordinate =
                new Coordinate(
                        response.pos(),
                        mapWidth
                );

        AgentType type =
                AgentType.fromValue(
                        response.kind()
                );

        return new Agent(
                agentId,
                type,
                coordinate,
                response.fuel()
        );
    }
}