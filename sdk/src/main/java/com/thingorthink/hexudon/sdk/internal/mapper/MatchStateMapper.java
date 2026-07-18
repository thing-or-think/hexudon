package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.*;
import com.thingorthink.hexudon.sdk.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Map<Coordinate, TrafficLevel> roadCondition = new HashMap<>();
        if (dto.roadCondition() != null) {
            for (Map.Entry<String, Integer> entry : dto.roadCondition().entrySet()) {
                int pos = Integer.parseInt(entry.getKey());
                Coordinate coord = new Coordinate(pos, mapWidth);
                TrafficLevel level = TrafficLevel.fromValue(entry.getValue());
                roadCondition.put(coord, level);
            }
        }

        Map<String, Team> teams = new HashMap<>();
        if (dto.teams() != null) {
            for (Map.Entry<String, TeamResponse> entry : dto.teams().entrySet()) {
                String teamId = entry.getKey();
                Team team = toTeam(teamId, entry.getValue(), mapWidth);
                teams.put(teamId, team);
            }
        }

        MatchStatus status = MatchStatus.fromString(dto.status());

        return new MatchState(
                dto.endsAt(),
                dto.day(),
                dto.stepsToday(),
                roadCondition,
                teams,
                status
        );
    }

    /**
     * Converts TeamResponse DTO into Team domain model.
     *
     * @param teamId assigned team identifier
     * @param dto team response
     * @param mapWidth board width
     * @return team domain model
     */
    public static Team toTeam(
            String teamId,
            TeamResponse dto,
            int mapWidth
    ) {
        Objects.requireNonNull(
                dto,
                "Team response must not be null"
        );

        List<Agent> agents = new java.util.ArrayList<>();
        if (dto.agents() != null) {
            for (AgentResponse agentDto : dto.agents()) {
                agents.add(toAgent(agentDto, mapWidth));
            }
        }

        List<String> distinctTypes = dto.distinctTypes() != null ? dto.distinctTypes() : java.util.Collections.emptyList();

        return new Team(
                teamId,
                agents,
                distinctTypes
        );
    }

    private static Agent toAgent(
            AgentResponse response,
            int mapWidth
    ) {
        int pos = response.pos() != null ? response.pos() : (response.cell() != null ? response.cell() : 0);
        Coordinate coordinate = new Coordinate(pos, mapWidth);

        AgentType type;
        Object typeObj = response.type() != null ? response.type() : response.kind();
        if (typeObj instanceof Number) {
            type = AgentType.fromValue(((Number) typeObj).intValue());
        } else if (typeObj instanceof String) {
            type = AgentType.fromString((String) typeObj);
        } else {
            type = AgentType.PATROL; // fallback
        }

        return new Agent(
                response.agentId(),
                type,
                coordinate,
                response.fuel()
        );
    }
}
