package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.entity.*;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.valueobject.ActionType;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.valueobject.AgentType;
import com.naprock.hexudon.domain.valueobject.TurnSimulationResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class MatchMapper {

    public MatchMapper() {
    }

    public TeamScoreResponse toTeamScoreResponse(TeamScore teamScore) {
        if (teamScore == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamScore must be not null."
            );
        }

        return new TeamScoreResponse(
                teamScore.getTeamId(),
                teamScore.getUniqueUdonTypesCount(),
                teamScore.getAccumulatedDailyUdonTypes(),
                teamScore.getTotalServings(),
                teamScore.getTotalResponseTimeMs()
        );
    }

    public MatchStateResponse toMatchStateResponse(MatchState state) {
        if (state == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "MatchState must be not null."
            );
        }

        return new MatchStateResponse(
                state.getStatus(),
                state.getCurrentTurn(),
                state.getTeams().stream().map(this::toTeamResponse).toList(),
                state.getGameMap().getCells().stream().map(this::toCellResponse).toList(),
                state.getGameMap().getSpots().stream().map(this::toSpotResponse).toList()
        );
    }

    public CellResponse toCellResponse(Cell cell) {
        if (cell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "cell must not be null."
            );
        }

        return new CellResponse(
                toCoordinateResponse(cell.getCoordinate()),
                cell.getTerrainType()
        );
    }

    public TeamResponse toTeamResponse(Team team) {
        if (team == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "team must not be null."
            );
        }

        return new TeamResponse(
                team.getTeamName(),
                team.getAgents().stream().map(this::toAgentResponse).toList(),
                team.isDisqualified(),
                team.getSpamViolationCount(),
                team.getCollectedUdon(),
                team.isSubmittedPlan()
        );
    }

    public SpotResponse toSpotResponse(Spot spot) {
        if (spot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "spot must not be null."
            );
        }

        return new SpotResponse(
                toCoordinateResponse(spot.getCoordinate()),
                spot.getUdonType(),
                spot.getTeamUdonStocks()
        );
    }

    public CoordinateResponse toCoordinateResponse(Coordinate coordinate) {
        if (coordinate == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "coordinate must not be null."
            );
        }

        return new CoordinateResponse(
                coordinate.getX(),
                coordinate.getY()
        );
    }

    public AgentResponse toAgentResponse(Agent agent) {
        if (agent == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Agent must not be null."
            );
        }

        AgentType type = agent instanceof PatrolAgent
                ? AgentType.PATROL
                : AgentType.REFUEL;

        return new AgentResponse(
                agent.getId(),
                type,
                toCoordinateResponse(agent.getCoordinate()),
                agent.getFuel(),
                agent.getRemainingSteps()
        );
    }

    public Action toAction(ActionRequest request) {
        if (request == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "ActionRequest can not be null."
            );
        }

        try {
            int order = request.order();

            Coordinate coordinate;
            if (request.actionType() == ActionType.MOVE) {
                coordinate = new Coordinate(
                        request.coordinate().x(),
                        request.coordinate().y())
                ;
            } else {
                coordinate = null;
            }

            long ts = System.currentTimeMillis();
            return new Action(
                    order,
                    request.actionType(),
                    coordinate,
                    ts
            );
        } catch (IllegalArgumentException e) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid action request: " + e.getMessage()
            );
        }
    }

    public Map<String, List<Action>> toDomainActionPlanMap(
            DayActionRequest dayRequest
    ) {
        Objects.requireNonNull(dayRequest, "dayRequest must not be null");

        return dayRequest.agentPlans()
                .stream()
                .collect(Collectors.toMap(
                        AgentActionPlanRequest::agentId,
                        plan -> plan.actions()
                                .stream()
                                .map(this::toAction)
                                .toList()
                ));
    }

    public ActionResponse toActionResponse(
            Action action
    ) {
        Objects.requireNonNull(action, "action must not be null");

        return new ActionResponse(
                action.getOrder(),
                action.getActionType(),
                toCoordinateResponse(action.getTargetCoordinate()),
                action.getTimestamp()
        );
    }

    public AgentActionPlanResponse toAgentActionPlanResponse(
            AgentExecutionResult executionResult
    ) {
        Objects.requireNonNull(executionResult, "executionResult must not be null");

        List<ActionResponse> actions = executionResult.actions()
                .stream()
                .map(this::toActionResponse)
                .toList();

        return new AgentActionPlanResponse(
                executionResult.agentId(),
                actions
        );
    }

    public DayActionResponse toDayActionResponse(
            TurnSimulationResult simulationResult
    ) {
        Objects.requireNonNull(simulationResult, "simulationResult must not be null");

        List<AgentActionPlanResponse> plans = simulationResult.agentExecutionResults()
                .stream()
                .map(this::toAgentActionPlanResponse)
                .toList();

        return new DayActionResponse(
                simulationResult.day(),
                plans
        );
    }

}
