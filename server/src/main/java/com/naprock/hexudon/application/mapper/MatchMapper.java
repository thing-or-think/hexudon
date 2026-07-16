package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.match.*;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.model.match.SubmitActionsCommand;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.*;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;

import java.util.List;

public final class MatchMapper {

    public static BoardConfigResponse toBoardConfigResponse(MatchConfig config) {
        return new BoardConfigResponse(
                toMapResponse(config.map()),
                config.spots().stream().map(MatchMapper::toSpotResponse).toList(),
                config.daySteps(),
                config.busyThreshold(),
                config.jammedThreshold()
        );
    }

    public static TeamResponse toTeamResponse(Team team, int mapWidth) {
        return new TeamResponse(
                team.getTeamId(),
                team.getAgents()
                        .stream()
                        .map(agent -> toAgentResponse(agent, mapWidth))
                        .toList()
        );
    }

    public static SubmitActionsCommand toDomainMap(SubmitActionRequest request) {
        return new SubmitActionsCommand(
                request.day(),
                request.actions()
                        .stream()
                        .map(actions -> actions.stream()
                                .flatMap(value -> MatchMapper.toActions(value).stream())
                                .toList())
                        .toList()
        );
    }

    public static List<Action> toActions(Integer value) {
        return Action.fromApiValue(value);
    }

    public static Coordinate toCoordinate(CoordinateRequest request) {
        if (request == null) {
            return null;
        }
        return new Coordinate(request.x(), request.y());
    }

    public static TeamRegistrationData toTeamRegistrationData(
            TeamRegisterRequest request
    ) {
        return new TeamRegistrationData(
                request.teamName(),
                request.types()
        );
    }

    public static MatchStateResponse toMatchStateResponse(
            MatchState state,
            String teamName,
            int width
    ) {
        if (state == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "state must not be null."
            );
        }

        if (teamName == null || teamName.isBlank()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamName must not be blank."
            );
        }

        if (width <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "width must be positive."
            );
        }

        Team currentTeam = state.requireTeam(teamName);

        return new MatchStateResponse(
                state.getTurnEndTime(),
                state.getCurrentTurn(),

                currentTeam.getAgents()
                        .stream()
                        .map(agent -> toAgentResponse(agent, width))
                        .toList(),

                state.getTeams()
                        .stream()
                        .filter(team -> !team.equals(currentTeam))
                        .map(team -> toTeamResponse(team, width))
                        .toList(),

                state.getTrafficHistory()
                        .getLatestTrafficFlows()
                        .stream()
                        .map(trafficFlow -> toTrafficResponse(trafficFlow, width))
                        .toList()
        );
    }

    public static AgentResponse toAgentResponse(Agent agent, int mapWidth) {
        if (agent == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "agent must not be null."
            );
        }

        return new AgentResponse(
                agent.getAgentType().getValue(),
                agent.getPosition().toIndex(mapWidth),
                agent.getFuel()
        );
    }

    public static TrafficResponse toTrafficResponse(TrafficFlow trafficFlow, int mapWidth) {
        if (trafficFlow == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "trafficFlow must not be null."
            );
        }

        return new TrafficResponse(
                trafficFlow.getCoordinate().toIndex(mapWidth),
                trafficFlow.trafficLevel().order()
        );
    }

    public static MatchConfigResponse toMatchConfigResponse(MatchConfig config) {

        if (config == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "config must not be null."
            );
        }

        return new MatchConfigResponse(
                config.startsAt(),
                config.daySeconds(),
                config.daySteps(),
                toMapResponse(config.map()),
                config.spots().stream().map(MatchMapper::toSpotResponse).toList(),
                config.agents(),
                config.fuelLimits(),
                config.players(),
                config.busyThreshold(),
                config.jammedThreshold()
        );
    }

    public static MapResponse toMapResponse(MapConfig map) {
        return new MapResponse(
                map.height(),
                map.width(),
                map.cells()
        );
    }

    public static SpotResponse toSpotResponse(SpotConfig spot) {
        if (spot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "spot must not be null."
            );
        }

        return new SpotResponse(
                spot.brand(),
                spot.pos(),
                spot.stocks()
        );
    }
}

