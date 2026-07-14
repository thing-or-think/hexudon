package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.match.*;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.dto.team.TeamScoreResponse;
import com.naprock.hexudon.application.model.match.MatchStateData;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.map.Spot;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MatchMapper {


    public static TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(
                team.getTeamName(),
                team.getAgents().stream().map(MatchMapper::toAgentResponse).toList());
    }

    public static Map<String, List<Action>> toDomainMap(SubmitActionRequest request) {
        return request.actions()
                .stream()
                .collect(Collectors.groupingBy(
                        ActionRequest::agentId,
                        Collectors.mapping(
                                MatchMapper::toAction,
                                Collectors.toList()
                        )
                ));
    }

    public static Action toAction(ActionRequest request) {
        return new Action(
                request.actionType(),
                toCoordinate(request.coordinate())
        );
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
                request.amountPatrol(),
                request.amountRefuel()
        );
    }

    public static MatchStateResponse toMatchStateResponse(MatchStateData stateData) {
        if (stateData == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "stateData must not be null."
            );
        }

        return new MatchStateResponse(
                stateData.status(),
                stateData.turn(),
                stateData.agents().stream()
                        .map(MatchMapper::toAgentResponse)
                        .toList(),
                stateData.trafficFlows().stream()
                        .map(MatchMapper::toTrafficResponse)
                        .toList(),
                stateData.spots().stream()
                        .map(MatchMapper::toSpotResponse)
                        .toList(),
                stateData.teamScores().stream()
                        .map(MatchMapper::toTeamScoreResponse)
                        .toList()
        );
    }

    public static TeamScoreResponse toTeamScoreResponse(TeamScore teamScore) {
        if (teamScore == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamScore must not be null."
            );
        }

        return new TeamScoreResponse(
                teamScore.getTeamName(),
                teamScore.getUniqueUdonTypesCount(),
                teamScore.getAccumulatedDailyUdonTypes(),
                teamScore.getTotalServings(),
                teamScore.getTotalResponseTimeMs()
        );
    }

    public static AgentResponse toAgentResponse(Agent agent) {
        if (agent == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "agent must not be null."
            );
        }

        return new AgentResponse(
                agent.getId(),
                toCoordinateResponse(agent.getPosition()),
                agent.getAgentType(),
                agent.getFuel(),
                agent.getRemainingSteps()
        );
    }

    public static TrafficResponse toTrafficResponse(TrafficFlow trafficFlow) {
        if (trafficFlow == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "trafficFlow must not be null."
            );
        }

        return new TrafficResponse(
                toCoordinateResponse(trafficFlow.getCoordinate()),
                trafficFlow.trafficLevel()
        );
    }

    public static MatchConfigResponse toMatchConfigResponse(GameMap gameMap, MatchConfig config) {

        if (gameMap == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "gameMap must not be null."
            );
        }

        if (config == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "config must not be null."
            );
        }

        return new MatchConfigResponse(
                config.mapWidth(),
                config.mapHeight(),
                gameMap.getCells().stream().map(MatchMapper::toCellResponse).toList(),
                gameMap.getSpots().stream().map(MatchMapper::toSpotResponse).toList(),
                config.agentsPerTeam(),
                config.maxFuel(),
                config.maxStepsPerTurn(),
                config.maxTurns()
        );
    }

    public static SpotResponse toSpotResponse(Spot spot) {
        if (spot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "spot must not be null."
            );
        }

        return new SpotResponse(
                toCoordinateResponse(spot.getCoordinate()),
                spot.getUdonType(),
                spot.getUdonAmount()
        );
    }

    public static CellResponse toCellResponse(Cell cell) {
        if (cell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "cell must not be null."
            );
        }

        return new CellResponse(
                toCoordinateResponse(cell.coordinate()),
                cell.terrainType()
        );
    }

    public static CoordinateResponse toCoordinateResponse(Coordinate coordinate) {
        if (coordinate == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "coordinate must not be null."
            );
        }

        return new CoordinateResponse(
                coordinate.x(),
                coordinate.y()
        );
    }
}

